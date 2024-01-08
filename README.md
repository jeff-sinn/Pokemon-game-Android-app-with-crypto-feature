# COMP3330

Run the project in step by step procedure:

1. Open the backend_server folder in vscode first.
2. When you have Python and PIP installed in your environment, run "py -m pip install flask" or "pip install flask" to install Flask required for /backend_server/main.py.
3. Then, In the terminal in vscode inside backend_server directory, type "python main.py" to run the backend server code to communcate with the pokemon.db. 
4. Then the terminal will show two server running url(Running on http...). And the correct server running URL will be the SECOND url displayed in the terminal. Every user will have different server address
5. Then open the whole project in Android Studio. We need to amend the server address variable in the project as every user have different server address in next step.
6. So COPY the SECOND url mentioned in step 3 to the file /app/src/main/res/values/strings.xml to change the original "server_ip" string value shown in the image below:
   

6.Save the change in server IP in strings.xml. Make sure the main.py file in backend_server folder is running. Then you can run the project by clicking "Run app" button in android studio. (Recommend run in Pixel XL)
