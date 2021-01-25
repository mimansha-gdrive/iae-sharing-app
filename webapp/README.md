# Drive Sharing App webapp

The web application contains the client side code to offer capabilities for:
1. Browse the Google Drive resources (folders/files) of the authenticated user.
2. Search for a file using any pattern of the file name. 
3. Transfer ownership of a resource (folder/file) to another user. Currently, the transfer-to user is
hardcoded. 

## Dependencies
All the dependencies are specified in `package.json`. Some of the noteworthly ones are: 
1. React for the Web application
2. React router for navigation 
3. Material-UI for various UI components used (TreeView in particular)
4. Fetch to interact with the server side API

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.


### `npm run build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.
This is used to integrate with the SpringBoot app by placing the status resources under Spring's static
directory. 

