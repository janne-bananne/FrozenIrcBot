FrozenIrcBot
============

Functionality
-------------

Configuration
-------------
```yaml
hostname:           irc.domain.tld
channel:            #channel
name:               nickname
auth_name:          authname
auth_password:      
auth_type:          None
wordnet_key:        
urban_key:          
wolframalpha_key:   
handler:            de.kuschku.ircbot.handlers.LinkTitleHandler,de.kuschku.ircbot.handlers.DefinitionHandler,de.kuschku.ircbot.handlers.TranslationHandler,de.kuschku.ircbot.handlers.WolframAlphaHandler
```

```yaml
hostname: irc.domain.tld
```
This specifies the domain of the IRC server that the bot should connect to.

```yaml
channel: #channel,#channel2,#channel3
```
This defines the channel the bot should try to join.

```yaml
name: IrcBot
auth_name: mybot
```
This is for defining the bot's name, it would show up as ```IrcBot <mybot@something.ip>``` here.

```yaml
auth_name:          authname
auth_password:      
auth_type:          None
```
This specifies the login data. auth\_type supports TheQBot (for QuakeNet) and NickServ (for most other networks). You'll have to specify auth\_password if you want to use authentication.

```yaml
wordnet_key:        
urban_key:          
wolframalpha_key:  
```
Here you can enter your API keys for the WordNet, UrbanDictionary and WolframAlpha API if you have some, they are used for the WolframAlpha–Handler and the Definition–Handler.

```yaml
handler:            de.kuschku.ircbot.handlers.LinkTitleHandler,de.kuschku.ircbot.handlers.DefinitionHandler,de.kuschku.ircbot.handlers.TranslationHandler,de.kuschku.ircbot.handlers.WolframAlphaHandler
```
With this feature you can specify the built-in handlers that should be loaded per default. If you'd like to add your own handlers, you can put them into the plugins/ folder. Just make sure that they extend ListenerAdapter<PircBotX>.

To-Do
-----

 - finish the Readme
