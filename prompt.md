We are building a simple website to store and share certificates.

There are two paths:
1. Certificate upload
2. Certificate view

User can upload a certificate and share it with the world. User first logs in and then uploads the certificate. 
The certificate is stored in a database and user can view all the certificates uploaded by them.
There must ba shareable link to view the certificate.
Lets just build this for a single user. That means, a user will set up the credentials one time during the application launch
and will that use that to signin later on.
The signin page should have a text field to enter the username and password and a recaptcha to verify that the user is not a robot.
Once logged in, there must be a home page to list all the certificates uploaded by the user with an image icon to preview the certificate and a link to edit the certificate.
The user can also delete the certificate.
The user should be able to upload one certificate at a time.
The certificate must be a single pdf/jpeg file not exceeding 15 megabytes in size.
There must be a share button to generate a shareable link to view the certificate.
For viewing a certification, no authentication is required, but a recaptcha to verify that the user is not a robot.

Lets build this application in java using Quarkus.
Lets deploy this application on aws, so lets create a deployable web application in zip file format so we can upload this to aws amplify.
Create tests for the application to make sure that the application is working as expected.
The frontend code should be in the src/main/web folder.
The frontend code should be in bootstrap latest version and any javascript library that best fits this application.
There must be API end points for the frontend to call. so we can eventually create a mobile application for the user to view the certificates.
The secrets can be stored in aws secrets manager.
The database can be created in aws dynamodb.
everything free tier on aws is enough for this application.
The ui should have material design incorporated.
The ui should be responsive.
The ui should be mobile friendly.
The ui should be easy to navigate.
Lets have just the letter 'C' as the logo with some simple design that goes well with the material design.

There must be a docker file and local mode so we can run the application locally and shell script to wrap up the flow and start the application. For local testing,
we can use a simple in-memory database and can be ephemeraly.

There must be a separate script to build and push the distributable zip file to aws amplify, so we can deploy the application to aws amplify.
The script must except the environment variables required and print the url of the application.

Lets take care of routing i.e. setting up route53 with a domain later on.