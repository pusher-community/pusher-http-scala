# pusher-http-scala
[![Build Status](https://travis-ci.org/vivangkumar/pusher-http-scala.svg?branch=master)](https://travis-ci.org/vivangkumar/pusher-http-scala)

A scala library to interact with the Pusher HTTP API.

This package lets you trigger events to your client and query the state of your Pusher channels.
When used with a server, you can validate Pusher webhooks and authenticate private- or presence-channels.

In order to use this library, you need to have a free account on http://pusher.com.
After registering, you will need the application credentials for your app.

### Table of Contents

- [Installation](#installation)
- [Getting Started](#getting-started)
- [Usage](#usage)
  - [Errors](#errors)
  - [Triggering events](#triggering-events)
  - [Excluding event recipients](#excluding-event-recipients)
  - [Authenticating Channels](#authenticating-channels)
  - [Application state](#application-state)
  - [Webhook validation](#webhook-validation)
  - [Custom Types](#custom-types)
- [Feature Support](#feature-support)
- [Developing the Library](#developing-the-library)
  - [Running the tests](#running-the-tests)
- [License](#license)

## Installation

```
libraryDependencies += "com.pusher" %% "pusher-http-scala" % <version>
```

where `<version>` is the version of the library you'd like to use.

## Getting Started

Check out `PusherExample.scala` inside the example folder. It has some basic code to get started quickly.

## Configuration

There easiest way to configure the library is by creating a new `Pusher` instance:

```scala
val pusher = new Pusher("YOUR_APP_ID", "YOUR_KEY", "YOUR_SECRET")
val result = pusher.trigger(List("test_channel"), "test_event", "test_event")
result match {
  case Left(error) => println(error)
  case Right(res) => println(res)
}
```

## Usage

### Errors

Note that this library does not throw any exceptions. A more functional approach is used by preferring the use of
`Either[Left, Right]` type. Return values are wrapped in a `PusherResponse` type which might contain either an error
on the left or the result on the right. It is then a case of using pattern matching to act on the result.

### Triggering events

It is possible to trigger an event on one or more channels. Channel names can contain only characters which are alphanumeric, `_` or `-`` and have to be at most 200 characters long. Event name can be at most 200 characters long too.

#### `trigger(channels: List[String], event: String, data: String, socketId: Option[String] = None)`

|Argument   |Description   |
|:-:|:-:|
|channels `String`   |The name of the channel you wish to trigger on.   |
|event `String` | The name of the event you wish to trigger |
|data `String` | The payload you wish to send. |
|socketId `Option[String]`| Optional socket id to event the recipient|

```scala
pusher.trigger(List("greeting_channel"), "say_hello", "{"hello": "world"}")
```

### Excluding event recipients

You can also exclude a recipient whose connection has that `socket_id` from receiving the event. You can read more [here](http://pusher.com/docs/duplicates).

```scala
pusher.trigger(List("a_channel"), "event", "hola", Some("123.12"))
```

### Authenticating Channels

Application security is very important so Pusher provides a mechanism for authenticating a user’s access to a channel at the point of subscription.

This can be used both to restrict access to private channels, and in the case of presence channels notify subscribers of who else is also subscribed via presence events.

This library provides a mechanism for generating an authentication signature to send back to the client and authorize them.

For more information see our [docs](http://pusher.com/docs/authenticating_users).

#### `authenticate(channel: String, socketId: String, customDatOpt: Option[PresenceUser]`)

|Argument|Description|
|:-:|:-:|
|channel `String`| The channel to authenticate|
|socketId `String`| The assigned socket id|
|customDataOpt `Option[PresenceUser]`| Presence user data (only for presence channels)|

Returns a JSON string like so `{"auth":"somekey:81a249f5aae14a7ffdef6574bc7aceeac10472f79e1b74283d3306d4a513bb89"}`

##### Private Channels

```scala
pusher.authenticate("private-test-channel", "123.12", None)
```

##### Presence Channels

```scala
pusher.authenticate("presence-channel-2", "123.12", Some(PresenceUser("123", Map("key" -> "value"))))
```

### Application state

This library allows you to query our API to retrieve information about your application's channels, their individual properties, and, for presence-channels, the users currently subscribed to them.

#### Get the list of channels in an application

##### `channelsInfo(prefixFilterOpt: Option[String], attributesOpt: Option[List[String]])`

|Argument|Description|
|:-:|:-:|
|prefixFilterOpt `Option[String]`| A map with query options. A key with `"filter_by_prefix"` will filter the returned channels. To get number of users subscribed to a presence-channel, specify an `"info"` key with value `"user_count"`.
|attributesOpt: `Option[List[String]]` | A list of attributes that you want returned for that channel|

Returns a `PusherResponse[ChannelsInfoResponse]`(`Either[PusherError, ChannelsInfoResponse]`).

```scala
val result = pusher.channelsInfo(Some("presence-"), Some("user_count"))
result match {
  case Left(error) => println(s"Yikes, there was an error: ${error.message}")
  case Right(res) => println(res) // Right(ChannelsInfoResponse(Map("presence-foobar" -> ChannelDetails(user_count: 1))))
}
```

#### Get the state of a single channel

##### `channelInfo(channel: String, attributes: Option[List[String]])`

|Argument|Description|
|:-:|:-:|
|channel `String`| The name of the channel|
|attributes `Option[List[String]]` | A list of attributes you'd like to request|

Returns a `PusherResponse[ChannelInfoResponse]`(`Either[PusherError, ChannelInfoResponse]`).

```scala
val result = pusher.channelInfo(channel: "some-channel", None)
result match {
  case Left(error) => println(s"Yikes, there was an error: ${error.message}")
  case Right(res) => println(res) // Right(ChannelInfoResponse(occupied = true, None, None))
}
```

#### Get a list of users in a presence channel

##### `usersInfo(channel: String)`

|Argument|Description|
|:-:|:-:|
|channel `String`| The channel name|

Returns a `PusherResponse[UserssInfoResponse]`(`Either[PusherError, UsersInfoResponse]`).

```scala
val result = pusher.usersInfo(channel: "some-channel")
result match {
  case Left(error) => println(s"Yikes, there was an error: ${error.message}")
  case Right(res) => println(res) // Right(UsersInfoResponse(List(UserDetails("1"))))
}
```

### Webhook validation

On your [dashboard](http://app.pusher.com), you can set up webhooks to POST a payload to your server after certain events. Such events include channels being occupied or vacated, members being added or removed in presence-channels, or after client-originated events. For more information see <https://pusher.com/docs/webhooks>.

This library provides a mechanism for checking that these POST requests are indeed from Pusher, by checking the token and authentication signature in the header of the request.

##### `validateWebhook(key: String, signature: String, body: String)`

|Argument|Description|
|:-:|:-:|
|key `String` | Key from the `X-Pusher-Key` header|
|signature `String` | Signature from the `X-Pusher-Signature` header|
|body `String`| Body of the webhook (JSON string) |

Returns a `PusherResponse[WebhookResponse]`(`Either[PusherError, WebhookResponse]`)

#### Custom Types

There are a number of custom types used and can be found in the `Types.scala` file [here](https://github.com/vivangkumar/pusher-http-scala/blob/master/src/main/scala/com/pusher/Types.scala)


## Feature Support

Feature                                    | Supported
-------------------------------------------| :-------:
Trigger event on single channel            | *&#10004;*
Trigger event on multiple channels         | *&#10004;*
Excluding recipients from events           | *&#10004;*
Authenticating private channels            | *&#10004;*
Authenticating presence channels           | *&#10004;*
Get the list of channels in an application | *&#10004;*
Get the state of a single channel          | *&#10004;*
Get a list of users in a presence channel  | *&#10004;*
WebHook validation                         | *&#10004;*
Heroku add-on support					   | *&#10008;*
Debugging & Logging                        | *&#10008;*
Cluster configuration                      | *&#10004;*
Timeouts                                   | *&#10008;*
HTTPS                                      | *&#10004;*
HTTP Proxy configuration                   | *&#10008;*
HTTP KeepAlive                             | *&#10008;*


#### Helper Functionality

These are helpers that have been implemented to to ensure interactions with the HTTP API only occur if they will not be rejected e.g. [channel naming conventions](https://pusher.com/docs/client_api_guide/client_channels#naming-channels).

Helper Functionality                     | Supported
-----------------------------------------| :-------:
Channel name validation 			     | &#10004;
Limit to 10 channels per trigger         | &#10004;
Limit event name length to 200 chars     | &#10004;

## Developing the Library

Feel more than free to fork this repo, improve it in any way you'd prefer, and send us a pull request :)

### Running the tests

Run tests using sbt
Simply type:

    $ sbt test

## License

This code is free to use under the terms of the MIT license.