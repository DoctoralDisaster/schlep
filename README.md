Schlep is a unified messaging framework which provides a single, simple API 
to send or receive messages from various messaging technologies.  The API
is heavily based on Guice utilizing MapBinder as the basis for the plugin
architecture.  A separate Guice module is provided for each messaging 
technology.

Key features
*  Policy driven batching
*  Policy driven retry
*  Send or receive decorator abstraction