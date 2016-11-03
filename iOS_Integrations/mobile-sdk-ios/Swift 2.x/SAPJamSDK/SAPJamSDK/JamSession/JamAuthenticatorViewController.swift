//
//  JamAuthenticatorViewController.swift
//  SAPJamSDK
//
//  Copyright © 2016 SAP SE. All rights reserved.
//

import UIKit
import WebKit
import OAuthSwift

class JamAuthenticatorViewController: UIViewController {
    
    private var webView: WKWebView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView = WKWebView()
        view = webView
        
        
        if (JamAuthConfig.isLoggedIn()) {
            goToMainApp()
        }
        else {
            beginOAuthLogin()
        }
    }
    
    private func goToMainApp () {
        let appDelegate: AppDelegate = UIApplication.sharedApplication().delegate as! AppDelegate
        let vc: UIViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewControllerWithIdentifier("mainApp")
        
        appDelegate.window!.rootViewController = vc
    }
    
    private func beginOAuthLogin () {
        // Use OAuthSwift library to authenticate with 3-legged OAuth1.0a
        let oauthswift = JamAuthConfig.sharedInstance.oauthswift!
        oauthswift.authorize_url_handler = SafariURLHandler(viewController: self)

        oauthswift.authorizeWithCallbackURL(
            NSURL(string: "sapjamsdk://oauth-callback/jam")!,
            success: { credential, response, parameters in
                JamAuthConfig.sharedInstance.storeCredentials(credential)
                self.goToMainApp()
            },
            failure: { error in
                print("Error occured during authorizeWithCallbackURL")
                print(error.localizedDescription)
            }             
        )
    }
    
    
}
