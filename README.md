# Xml Entity Sanitizer

Resolve character entities in XML flight and maintenance manuals.
This tool is used to search and fix special characters in well-formed xml files before they are sent for contenta upload. 
The following command MUST be executed inside the folder contains app.properties. By default , you should just run it inside this downloawed folder.

## Installation

download `xmlEntitySantinizer.zip`  and unzip it. 

## Usage

From the unziped folder of `xmlEntitySantinizer.zip` , use sanitize.bat command  to clean up special characters in a xml and create a new xml file base on source xml file by adding "_sanitized" in file name. 

1. open windows command line console
2. go to the folder that hold the unzipped xmlEntitySantinizer
3. run sanitizer.bat as :

For exmaple, an xml file O:\dev\ATAIntegrationTesting\CH604PROD\MFM\GEN_MFM_20160203-004821\GEN_MFM_20160203-004821.xml , if we find it needs to be fixed. we can run this command :
sanitize.bat  O:\dev\ATAIntegrationTesting\CH604PROD\MFM\GEN_MFM_20160203-004821\GEN_MFM_20160203-004821.xml 

If you need to fix applicRefIds by reference an cirs.xml , the command canbe :
sanitize.bat  O:\dev\ATAIntegrationTesting\CH604PROD\MFM\GEN_MFM_20160203-004821\GEN_MFM_20160203-004821.xml cirs.xml

We should be able to find O:\dev\ATAIntegrationTesting\CH604PROD\MFM\GEN_MFM_20160203-004821\GEN_MFM_20160203-004821_sanitized.xml created. 

# Add new entities

We use app.properties to map special character with entities.If there is any entities that is not defined in app.properties , a warning message will show . for exmaple :
C:\Users\K0654384\tools\xmlEntityFixer\java>java  XMLCharacterValidator O:\dev\ATAIntegrationTesting\CH604PROD\SDS604_05_50\SDS_SDS_DX_20221027-141824\SDS_SDS_DX_20221027-141824-final.xml app.properties
Warning: Character not in pre-defined valid set: ? #9652

In this case , you need to add #9652 into app.properties.
 App.properties , each entity is mapped in format :ASCII_####=ENTITIES. For example :
 ASCII_181=&micro;
