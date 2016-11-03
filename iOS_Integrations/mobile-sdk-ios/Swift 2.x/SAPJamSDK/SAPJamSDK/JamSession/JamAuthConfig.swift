//
//  JamAuthConfig.swift
//  SAPJamSDK
//
//  Copyright © 2016 SAP SE. All rights reserved.
//

import Foundation
import OAuthSwift
import KeychainAccess

// Keys for storage of OAuth 1.0a single use access token and secret in keychain
let kAccessToken = "accessToken"
let kAccessSecret = "accessSecret"

public class JamAuthConfig {
    
    // Sets authentication to OAuth 1.0a
    public var oauthswift: OAuth1Swift?
    
    // Used by the "configure" method to set the OAuth 1.0a authentication server name
    private var serverName = ""
    
    // Sets the keychain domain
    private let keychain = Keychain(service: "com.sap.jam")
    
    // Creates a shared instance of the keychain
    static let sharedInstance = JamAuthConfig()
    
    // Checks if user is logged in by checking if the OAuth 1.0a token and secret have been set in the keychain
    public class func isLoggedIn() -> Bool {
        return sharedInstance.keychain[kAccessToken] != nil && sharedInstance.keychain[kAccessSecret] != nil
    }
    
    // Provides functions access to a successfully created OAuth 1.0a single use access token
    public typealias SingleUseTokenSuccessHandler = (singleUseToken: String) -> Void
    
    // Displays an error message in the console
    public typealias ErrorHandler = (error: NSError) -> Void
    
    // Gets the OAuth 1.0a single use access token from the authorization server so the app can access the Jam OData API
    public func getSingleUseToken(success: SingleUseTokenSuccessHandler, failure: ErrorHandler?) {
        let urlStr = getServerUrl() + "/v1/single_use_tokens"
    
        // Attempts to gets the OAuth 1.0a single use access token from the authorization server
        oauthswift!.client.post(urlStr,
            success: {
                data, response in
        
                // Converts response object into a string
                let dataString: NSString = NSString(data: data, encoding: NSUTF8StringEncoding)!
                
                // Creates a regular expression used to find the single use access token key-value pair in the string
                let regex: NSRegularExpression = try! NSRegularExpression(pattern: "(?<=<single_use_token id=\")[^\"]+", options:[.CaseInsensitive])
                
                // Executes the regular expression to determine if the single use access token is in the string
                if let match = regex.firstMatchInString(dataString as String, options:[], range: NSMakeRange(0, dataString.length)) {
                    
                    // Assigns the single use access token value from the key-value pair to a string
                    let token:String = dataString.substringWithRange(match.range)
                    
                    // Assigns the OAuth 1.0a single use access token to a publicly accessible property so
                    // other functions can access the SAP Jam OData API.
                    success(singleUseToken: token)
                }
                else {
                    
                    // Displays error information when a single use access token cannot be found in the string
                    let errorInfo = [NSLocalizedFailureReasonErrorKey: NSLocalizedString("Could not get Single Use Token", comment: "Response from /v1/single_use_tokens does not contain a token")]
                    failure?(error: NSError(domain: "sapjam.error", code: -1, userInfo: errorInfo))
                }

            },
            failure: { error in
                print(error)
                failure?(error: error)
            }
        )
    }

    // Creates the OAuth 1.0a authentication server URL
    public func getServerUrl() -> String {
        return "https://" + serverName
    }
    
    // Configures the URLs required to perform OAuth 1.0a authentication and authorization to get a single use
    // access token.
    public func configure(server: String, key: String, secret: String, companyDomain: String? = nil) {
        serverName = server
        let serverUrl = getServerUrl()
        
        validateServer()
        
        // Creates authorization url for normal oauth clients
        var authorizeUrl = serverUrl + "/oauth/authorize"
        
        if let domain = companyDomain {
            authorizeUrl = serverUrl + "/c/" + domain + "/oauth/authorize"
        }
        
        oauthswift = OAuth1Swift(
            consumerKey: key,
            consumerSecret: secret,
            requestTokenUrl: serverUrl + "/oauth/request_token",
            authorizeUrl:    authorizeUrl,
            accessTokenUrl:  serverUrl + "/oauth/access_token"
        )
        
        // Uses the existing single use token and secret in the keychain if the user is already logged in
        if JamAuthConfig.isLoggedIn() {
            oauthswift?.client.credential.oauth_token = keychain[kAccessToken]!
            oauthswift?.client.credential.oauth_token_secret = keychain[kAccessSecret]!
        }

    }
    
    // Validates the OAuth 1.0a authentication server domains
    private func validateServer() {
        // Note: this list may be subject to change (additions) in future.
        // Please use the server your company is registered with.
        let VALID_JAM_SERVERS = [
            "developer.sapjam.com",
            "jam4.sapjam.com",
            "jam8.sapjam.com",
            "jam10.sapjam.com",
            "jam12.sapjam.com",
            "jam2.sapjam.com",
            "jam15.sapsf.cn",
            "jam17.sapjam.com",
            "jam18.sapjam.com"]
        
        precondition(VALID_JAM_SERVERS.contains(serverName), "Must set a valid Jam server")
    }
    
    // Stores OAuth 1.0a single use token and secret in the keychain
    public func storeCredentials(credential: OAuthSwiftCredential) {
        keychain[kAccessToken] = credential.oauth_token
        keychain[kAccessSecret] = credential.oauth_token_secret
    }

}
