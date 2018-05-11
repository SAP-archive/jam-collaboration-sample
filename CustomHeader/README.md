# SAP Jam Collaboration Custom Header Sample

![alt text](custom_header_sample_screenshot.png "Custom Header Sample Screenshot")

# Introduction
This sample is a custom header that provides an interactive branded multilingual experience for SAP Jam Collaboration. It uses HTML, CSS, Bootstrap, custom Javascript libraries, and a stock-ticker gadget to accomplish this. The stock-ticker gadget also uses a third-party library that gets stock-ticker data from a third-party service (Alpha Vantage - https://www.alphavantage.co).

Use of the Alpha Vantage library and service is subject to applicable terms and conditions set out by Alpha Vantage. SAP does not make any representations or warranties respecting the Alpha Vantage library and service and SAP is not liable to you or any third party in respect of any use of the Alpha Vantage library and service.

To use this sample simply:
* Copy and paste this into the custom header of your SAP Jam Collaboration instance.
* Follow the in-line documentation in the sample.
* Use the Custom Header reference documentation located here:
  * Configure the Branding and Support options - https://help.sap.com/viewer/u_admin_help/b1cf4e797d4a1014ba05827eb0e91070.html
  * Best practices for custom headers - https://help.sap.com/viewer/u_admin_help/4099c60a71684aa18124604a1a4fe3a6.html


# Custom Header Sample Documentation

## Setup the Custom Header:
* Login to SAP Jam Collaboration as a company admin.
* Click on the "cog icon" and select "Admin" > "Branding" > "Web". The "Branding and Support" screen appears.
* Select the "Enable custom header" checkbox. The "Enable Javascript" checkbox appears.
* Select the “Enable JavaScript” checkbox.
  * Note: If “Enable JavaScript” is unchecked, SAP Jam Collaboration will remove all Javascript tags and html 
  element attributes from the custom header code.
* Deselect the "Show standard navigation menus" checkbox if you do not want to use SAP Jam Collaboration's header menus.
* Replace href links pointing to "#link_to_..." with URLs in your SAP Jam Collaboration instance.

## Set up the Stock Gadget (optional):
* Go to the documentation at the bottom of translated_custom_header.html

## Tips:
* Avoid the inclusion of scripts and external resources that would block or slow down overall performance.
* You can avoid code conflicts with your SAP Jam Collaboration instance by not:
  * using or interacting with SAP Jam Collaboration Javascript global variables and functions.
  * invoking undocumented internal JavaScript functions.
  * adding CSS or Javascript that:
    * affects elements within <jam-*> tags.
    * affects elements outside of the header tag.
* Ensure you add a prefix to all the IDs and class names for elements in the header.
  * Note: All names are prefixed with "header-sample-" in this code sample.

## Add multilingual support to your custom header:
* Description: Text that is substituted with translations based on user's language.
* How it works: Text in the detected langauge is obtained from the custom header JSON object by matching the key specified in the <jam-string> custom header html tag with the key in the custom header JSON object. If the language cannot be matched, then the "default-language" key is used.
* Usage:
  * div tag: `<jam-string key="{key}"></jam-string>`
  * placeholder: `<input placeholder=jam-string:{key}></input>`
* Example 1 - Create a multilingual dropdownMenuButton for "Welcome" in English, German, and Chinese:
  * key: "welcome"
  * div tag: `<jam-string key="welcome"></jam-string>`
  * Multilingual text:
    * English = "Welcome"
    * German = "Herzlich willkommen"
    * Chinese (Simplified) = "欢迎"
  * Language Codes and languages:
    * en = English
    * de = German
    * zh-CN = Chinese (Simplified)
* Example 2 - Create multilingual placholder text for an input box in English, German, and Chinese:
  * key: "find_everything"
  * placeholder: `<input id="header-sample-search" placeholder="jam-string:find_everything" />`
  * Multilingual text:
    * English = "Find Everything..."
    * German = "Finde alles ..."
    * Chinese (Simplified) = "找到一切..."
  * Language Codes and languages:
    * en = English
    * de = German
    * zh-CN = Chinese (Simplified)

### Example 1 - multilingual dropdownMenuButton
```
<li>
  <div class="header-sample-dropdown">
    <div class="header-sample-dropdown-toggle" data-header-sample-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="font-weight: bold">
      <jam-string key="welcome"></jam-string>
      <img src="/sample/custom_header/icon-caret-down.png" /> 
    </div>
  </div>
</li>
```

### Example 2 - multilingual placeholder
```
<div class="header-sample-hidden-xs" style="flex: 3; position: relative; margin: 0 10px;">
  <a id="header-sample-search-icon" aria-hidden="true" tabindex="-1" href="javascript:void(0);" data-type="search">
    <img src="/sample/custom_header/icon-search.png" />
  </a>
  <input id="header-sample-search" placeholder="jam-string:find_everything" />
</div>
```

### Example 1 and 2 - custom header JSON object
```
{
  "en": {
    "welcome": "Welcome",
    "find_everything": "Find Everything..."
  },
  "de": {
    "welcome": "Herzlich willkommen",
    "find_everything": "Finde alles ..."
  },
  "zh-CN": {
    "welcome": "欢迎",
    "find_everything": "找到一切..."
  },
  "default-language": "en"
}
```

# Reference
## Custom header JSON object 
The custom header JSON object (custom_header_translation.json) is structured as follows:
```
{
  "{language_1}": {
    "{key_1}": "{value}",
    "{key_2}": "{value}"
  },
  "{language_2}": {
    "{key_1}": "{value}",
    "{key_2}": "{value}"
  },
  "{language_2}": {
    "{key_1}": "{value}",
    "{key_2}": "{value}"
  },
  "default-language": "en"
}
```


## Languages and Language Codes
Add new languages by modifying the custom header JSON object with the following language codes:

| Language                              | Language Code |
| ------------------------------------- | ------------- |
| Arabic                                | ar-SA         |
| English (US)                          | en            |
| Bulgarian                             | bg-BG         |
| Catalan                               | ca-ES         |
| Chinese (Simplified)                  | zh-CN         |
| Chinese (Traditional)                 | zh-TW         |
| Croatian                              | hr-HR         |
| Czech                                 | cs-CZ         |
| Danish                                | da            |
| Dutch (Netherlands)                   | nl            |
| English (UK)                          | en-GB         |
| Finnish                               | fi-FI         |
| French (Canadian)                     | fr-CA         |
| French (France)                       | fr            |
| German (Germany)                      | de            |
| German (Swiss)                        | de-CH         |
| Greek (Greece)                        | el-GR         |
| Hebrew                                | he-IL         |
| Hindi                                 | hi            |
| Hungarian                             | hu            |
| Indonesian                            | id            |
| Italian                               | it            |
| Japanese                              | ja            |
| Korean                                | ko            |
| Malaysian                             | ms            |
| Norwegian (Bokmål)                    | nb-NO         |
| Polish                                | pl            |
| Portuguese (Brazil)                   | pt-BR         |
| Portuguese (Portugal)                 | pt-PT         |
| Romanian                              | ro            |
| Russian                               | ru            |
| Serbian (Serbia)                      | sr-RS         |
| Slovak                                | sk-SK         |
| Slovenian                             | sl-SI         |
| Spanish (Mexico)                      | es-MX         |
| Spanish (Spain)                       | es            |
| Swedish                               | sv-SE         |
| Thai                                  | th            |
| Turkish                               | tr-TR         |
| Ukrainian                             | uk-UA         |
| Vietnamese                            | vi-VN         |
| Welsh                                 | cy-GB         |



# License
Copyright 2014, SAP AG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


