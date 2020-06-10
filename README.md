<p align="center">
  <img height="300" src="logo.png">
</p>

# ScreenParty

**_ScreenParty_** is an app that allows you to align three Android smartphones and use their screens as one whole single screen.

## Requirements
In order to use ScreenParty, there are a two requirements that must be fulfilled:

  * All three devices must be connected to the same Wi-Fi network.
  * All three devices must have in storage the video you want to play
  * Video formats are limited to the [MediaPlayer supported formats](https://developer.android.com/guide/topics/media/media-formats#video-codecs).

There are no restriction to the aspect ratio of the video, although better results are achieved with a 16:9 ratio.
## Usage
As you start **_ScreenParty_** you have two options: you can either [host a new party](#host_a_new_party) or you can [join an existing one](#join_an_existing_party).
In both cases the users must select the video to play, so make sure you select the same video on all three devices.

### Host a new party
As you select _Host a new party_, after choosing the video to play, you will see:
  * Your IP address. This address will be used by other devides to join your party.
  * An overview of the connected devices.
  * The _Next_ button.
After the other two devices have succesfully joined your party, the _Next_ button will highlight. By pressing it, all three devices will proceed to the [alignment phase](#alignment_phase).

### Join an existing party
As you select _Join an existing party_, after choosing the video to play, you will see:
  * An input text bar. The IP address of the host device must be inserted here.
  * The _Connect_ button.
After the IP address have been typed, the _Connect_ button will highlight. By pressing it, if the address is correct, under the text bar will be displayed "Connected!". 
At this point you must wait for the host to proceed.

### Alignment phase
At this stage, the three devices must be aligned as shown by the arrows, which will appear on the side of the screen. The host will always be in the central position. In the host device's screen will appear a _Start!_ button. By pressing it, all three devices will start play the video.

### Playback phase
At this stage, the devices should all be playing the video simultaneously. The playback controls are avaible on the host's screen.
