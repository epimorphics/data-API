data-API
========

Design and implementation of the data services api.

## Building

Depends on snapshot versions of lib and appbase. You many need to install those locally if the releases are not up to date.

To build this project with mvn do this from the top level. Separate builds of the subprojects will fail.

## Deployment

A minimal vagrant deployment script for the app is included in the data-api-app project under directory deploy.

To provision a local VirtualBox instance change to the directory. 

    export MAVEN_PASSWORD=user:password
    vagrant up local
    
When the provisioning the completes the port localhost:4567 should be tunneled through to the running webapp (via an nginx frontend).

An AWS deployment is defined but not tested.

Notes on the current configuration (this can be changed in the web.xml and app.conf)

   * The configuration file is in the webapp - WEB-INF/app.conf. It would be externalized for easy reconfiguration.

   * Data files are loaded from /opt/dsapi/data, the deployment includes the same games data set.
   
   * The data set configurations are (dynamically) loaded from /opt/dsapi/conf, an example data set definition for the games data is included in the deployment.
   
   