# SmsMatrix
A simple SMS (text message) &lt;--> Matrix bridge.

This app bridges all sms messages to matrix. For every (new) text conversation contact the bot will open a private 1:1 room and sends the incoming messages to that room. Any messages sent in that room by the matrix user will then be sent to the contact via SMS.  
As currently there is no end-to-end encryption implemented in this app, for privacy reasons it is preferable to use it with your own Matrix server. Your data-in-transit however will always be safe as they are encrypted using https over matrix -- provided the Matrix server is configured properly.

# Set up
- Create a acount on a homeserver
- Install this app
- Give the app the app permission
- Type in the needed info in the app
  - Bot Username: is the username of the just created user
  - Bot Password: is the password of the just created user
  - Homeserver url: is the url of the homeserver where the user is created
  - Your username: is where you want to forward all yous text message to
  - Devicename: is the devicename wich the api will use
- Press save
- Now all your text messages will be bridged
