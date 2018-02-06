//
//  GroupsListViewController.swift
//  SAPJamSDK
//
//  Copyright © 2016 SAP SE. All rights reserved.
//

import UIKit
import OAuthSwift

class GroupsListViewController: UITableViewController {
    
    private var groups: NSArray = []

    override func viewDidLoad() {
        super.viewDidLoad()
        
        loadGroupsFromServer()
    }
    
    private func loadGroupsFromServer() {
        
        let jamSession = JamAuthConfig.sharedInstance
        let urlStr = jamSession.getServerUrl() + "/api/v1/OData/Groups?$select=Id,Name"
        _ = jamSession.oauthswift!.client.get(urlStr,
            headers: ["Accept": "application/json"],
            success: {
                response in
                
                do {
                    let jsonResult = try JSONSerialization.jsonObject(with: response.data, options: JSONSerialization.ReadingOptions.mutableContainers) as! [String: Any]
                
                    // Display the list of groups
                    let d = jsonResult["d"] as! [String: Any]
                    self.groups = d["results"] as! NSArray
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
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return groups.count
        }
        return 0
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let group = self.groups[indexPath.row] as! [String: Any]
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell")
        cell?.textLabel?.text = group["Name"] as? String
        
        return cell!
    }
}
