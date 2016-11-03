//
//  GroupsListViewController.swift
//  SAPJamSDK
//
//  Copyright © 2016 SAP SE. All rights reserved.
//

import UIKit
import OAuthSwift

class GroupsListViewController: UITableViewController {
    
    private var groups = []

    override func viewDidLoad() {
        super.viewDidLoad()
        
        loadGroupsFromServer()
    }
    
    private func loadGroupsFromServer() {
        
        let jamSession = JamAuthConfig.sharedInstance
        let urlStr = jamSession.getServerUrl() + "/api/v1/OData/Groups?$select=Id,Name"
        jamSession.oauthswift!.client.get(urlStr,
            headers: ["Accept": "application/json"],
            success: {
                data, response in
                
                do {
                    let jsonResult: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
                
                    // Display the list of groups
                    self.groups = jsonResult["d"]!["results"] as! NSArray
                    self.tableView.reloadData()
                } catch let error {
                    print(error)
                }
            },
            failure: { error in
                print(error)
            }
        )
        
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return groups.count
        }
        return 0
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let group = self.groups[indexPath.row]
        
        let cell = tableView.dequeueReusableCellWithIdentifier("cell")
        cell?.textLabel?.text = group["Name"]! as? String
        
        return cell!
    }

}

