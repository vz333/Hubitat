/**
 *  Copyright 2015 SmartThings
 *
 *  name: "Life360 User", namespace: "tmleafs", author: "tmleafs"
 *
 *	BTRIAL DISTANCE AND SLEEP PATCH 29-12-2017
 *	Updated Code to handle distance from, and sleep functionality
 *
 *	TMLEAFS REFRESH PATCH 06-12-2016 V1.1
 *	Updated Code to match Smartthings updates 12-05-2017 V1.2
 *	Added Null Return on refresh to fix WebCoRE error 12-05-2017 V1.2
 *	Added updateMember function that pulls all usefull information Life360 provides for webCoRE use V2.0
 *	Changed attribute types added Battery & Power Source capability 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Life360-User
 *
 *  Author: jeff
 *  Date: 2013-08-15
 *
 * ---- End of Original Header ----
 *
 *  V1.0.9 - 07/13/19 - Code touchups by cwwilson08
 *  V1.0.8 - 07/12/19 - Minor changes, code cleanup
 *  V1.0.7 - 07/10/19 - Added code for dashboard tiles, Info and Places tiles
 *  V1.0.6 - 07/10/19 - Added a Dashboard Tile
 *  V1.0,5 - 07/08/19 - Added Avatar and code cleanup (cwwilson08)
 *  V1.0.4 - 07/07/19 - Removed clientID field, no longer needed
 *  V1.0.3 - 07/03/19 - Changed booleans to strings so it'll work with RM. Thanks @doug
 *  V1.0.2 - 07/02/19 - Added clientID, updated namespace/author so if something goes wrong people know who to contact.
 *  V1.0.1 - 06/30/19 - Added code to turn debug logging on and off (bptworld)
 *  V1.0.0 - 06/30/19 - Initial port of driver for Hubitat (bptworld)
 */
 
import java.text.SimpleDateFormat

preferences {
	input title:"Distance", description:"This feature allows you change the display of distance to either Miles or KM. Please note, any changes will take effect only on the NEXT update or forced refresh.", type:"paragraph", element:"paragraph"
	input name: "units", type: "enum", title: "Distance Units", description: "Miles or Kilometers", required: false, options:["Kilometers","Miles"]
input "life360Paid", "bool", title: "Version of Life360 (off=Free, on=Paid)", required: true, defaultValue: false
input("avatarFontSize", "text", title: "Avatar Font Size", required: true, defaultValue: "15")
input("avatarSize", "text", title: "Avatar Size by Percentage", required: true, defaultValue: "75")

input("numOfLines", "number", title: "How many lines to display on History Tile (from 1 to 10 only)", required:true, defaultValue: 5)
input("historyFontSize", "text", title: "History Font Size", required: true, defaultValue: "15")
input("historyHourType", "bool", title: "Time Selection for History Tile (Off for 24h, On for 12h)", required: false, defaultValue: false)
input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: false
} 
 
metadata {
	definition (name: "Life360 User", namespace: "BPTWorld", author: "Bryan Turcotte", importURL: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Ported/Life360/L-driver.groovy") {
	capability "Presence Sensor"
	capability "Sensor"
capability "Refresh"
	capability "Sleep Sensor"
capability "Battery"
capability "Power Source"

	attribute "distanceMetric", "Number"
   	attribute "distanceKm", "Number"
	attribute "distanceMiles", "Number"
attribute "prevAddress1", "String"
attribute "prevAddress2", "String"
	attribute "address1", "String"
  	attribute "address2", "String"
  	attribute "battery", "number"
   	attribute "charge", "boolean" //boolean
   	attribute "lastCheckin", "number"
   	attribute "inTransit", "String" //boolean
   	attribute "isDriving", "String" //boolean
   	attribute "latitude", "number"
   	attribute "longitude", "number"
   	attribute "since", "number"
   	attribute "speedMetric", "number"
    attribute "speedMiles", "number"
    attribute "speedKm", "number"
   	attribute "wifiState", "boolean" //boolean
    attribute "savedPlaces", "map"
    attribute "avatar", "string"
    attribute "avatarHtml", "string"
    attribute "life360Tile1", "string"
    attribute "history", "string"
    attribute "status", "string"

	command "refresh"
	command "asleep"
command "awake"
command "toggleSleeping"
command "setBattery",["number","boolean"]
command "sendHistory", ["string"]
command "historyClearData"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}
}

def sendLife360Tile1() {
if(logEnable) log.debug "in Life360 User - Making the Avatar Tile"
def avat = device.currentValue('avatar')
def add1 = device.currentValue('address1')
def unit = device.currentValue('units')
def bThere = device.currentValue('since')
def bLevel = device.currentValue('battery')
def bCharge = device.currentValue('powerSource')
def bSpeedKm = device.currentValue('speedKm')
def bSpeedMiles = device.currentValue('speedMiles')
if(unit == "Kilometers") {
    bUnitsa = "${bSpeedKm} KMH"
} else {
    bUnitsa = "${bSpeedMiles} MPH"
}

def binTransit = device.currentValue('inTransit')
if(binTransit == "true") {
    binTransita = "Moving"
} else {
    binTransita = "Not Moving"
}

def bWifi = device.currentValue('wifiState')
if(bWifi == "true") {
    bWifiS = "Wifi"
} else {
    bWifiS = "No Wifi"
}

int sEpoch = device.currentValue('since')
theDate = use( groovy.time.TimeCategory ) {
    new Date( 0 ) + sEpoch.seconds
}
SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E hh:mm a")
String dateSince = DATE_FORMAT.format(theDate)

if(life360Paid) {
	tileMap = "<table width='100%' valign='top'>"
    tileMap += "<tr><td width='25%'><img src='${avat}' height='${avatarSize}%'></td>"
    tileMap += "<td width='75%'><p style='font-size:${avatarFontSize}px'>At: ${add1}<br>Since: ${dateSince}<br>${device.currentValue('status')}<br>${binTransita} - ${bUnitsa}<br>Phone Lvl: ${bLevel} - ${bCharge} - ${bWifiS}</p></td>"
    tileMap += "</tr></table>"
} else {  // Free
    tileMap = "<table width='100%' valign='top'>"
    tileMap += "<tr><td width='25%'><img src='${avat}' height='${avatarSize}%'></td>"
    tileMap += "<td width='75%'><p style='font-size:${avatarFontSize}px'>At: ${add1}<br>Since: ${dateSince}<br>${device.currentValue('status')}</p></td>"
    tileMap += "</tr></table>"
}

	state.tileDevice1Count = tileMap.length()
	if(state.tileDevice1Count <= 1000) {
		if(logEnable) log.debug "tileMap - has ${state.tileDevice1Count} Characters<br>${tileMap}"
	} else {
		state.rtileDevice1 = "Too many characters to display on Dashboard (${state.tileDevice1Count})"
	}
	sendEvent(name: "life360Tile1", value: tileMap, displayed: true)
}

def sendHistory(historyLog) {
if(logEnable) log.debug "In Life360 User - Making the History Tile"
	if(logEnable) log.debug "Life360 User - New item - ${historyLog}"

def hMessage = "${historyLog}"
	
	// Read in the maps
	try {
		sOne = state.speechMap1.get(state.s,nMessage)
		sTwo = state.speechMap2.get(state.s,nMessage)
		sThree = state.speechMap3.get(state.s,nMessage)
		sFour = state.speechMap4.get(state.s,nMessage)
		sFive = state.speechMap5.get(state.s,nMessage)
		sSix = state.speechMap6.get(state.s,nMessage)
		sSeven = state.speechMap7.get(state.s,nMessage)
		sEight = state.speechMap8.get(state.s,nMessage)
		sNine = state.speechMap9.get(state.s,nMessage)
		sTen = state.speechMap10.get(state.s,nMessage)
	}
	catch (e) {
    //log.error "Error:  $e"
}
	
	if(logEnable) log.debug "What Did I Say - OLD -<br>sOne: ${sOne}<br>sTwo: ${sTwo}<br>sThree: ${sThree}<br>sFour: ${sFour}<br>sFive: ${sFive}"
	
	if(sOne == null) sOne = "${state.nMessage}"
	if(sTwo == null) sTwo = "${state.nMessage}"
	if(sThree == null) sThree = "${state.nMessage}"
	if(sFour == null) sFour = "${state.nMessage}"
	if(sFive == null) sFive = "${state.nMessage}"
	if(sSix == null) sSix = "${state.nMessage}"
	if(sSeven == null) sSeven = "${state.nMessage}"
	if(sEight == null) sEight = "${state.nMessage}"
	if(sNine == null) sNine = "${state.nMessage}"
	if(sTen == null) sTen = "${state.nMessage}"
	
	// Move all messages down 1 slot
	mTen = sNine
	mNine = sEight
	mEight = sSeven
	mSeven = sSix
	mSix = sFive
	mFive = sFour
	mFour = sThree
	mThree = sTwo
	mTwo = sOne
	
	getDateTime()
mOne = "${state.newdate} - ${hMessage}"
	
	if(logEnable) log.debug "What Did I Say - NEW -<br>mOne: ${mOne}<br>mTwo: ${mTwo}<br>mThree: ${mThree}<br>mFour: ${mFour}<br>mFive: ${mFive}"
	
	// Fill the maps back in
	try {
		state.speechMap1.put(state.s,mOne)
		state.speechMap2.put(state.s,mTwo)
		state.speechMap3.put(state.s,mThree)
		state.speechMap4.put(state.s,mFour)
		state.speechMap5.put(state.s,mFive)
		state.speechMap6.put(state.s,mSix)
		state.speechMap7.put(state.s,mSeven)
		state.speechMap8.put(state.s,mEight)
		state.speechMap9.put(state.s,mNine)
		state.speechMap10.put(state.s,mTen)
	}
	catch (e) {
    //log.error "Error:  $e"
}
	
	historyLog = "<table width='100%'><tr><td align='left'>"
	if(numOfLines == 1) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}</p>"
	}
	if(numOfLines == 2) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}</p>"
	}
	if(numOfLines == 3) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}</p>"
	}
	if(numOfLines == 4) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}</p>"
	}
	if(numOfLines == 5) {
    historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}</p>"
	} 
	if(numOfLines == 6) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}</p>"
	}
	if(numOfLines == 7) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}</p>"
	}
	if(numOfLines == 8) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}</p>"
	}
	if(numOfLines == 9) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}</p>"
	}
	if(numOfLines == 10) {
		historyLog+= "<p style='font-size:${historyFontSize}px'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}<br>${mTen}</p"
	}
	historyLog+= "</td></tr></table>"
	
	state.historyTileCount = historyLog.length()
	if(state.historyTileCount <= 1000) {
		if(logEnable) log.debug "Life360 History - ${state.historyTileCount} Characters<br>${historyLog}"
	} else {
		historyLog = "Too many characters to display on Dashboard"
	}
if(logEnable) log.debug "History Log: ${historyLog}"
	sendEvent(name: "history", value: historyLog, displayed: true)
}

def installed(){
log.info "Life360 User Installed"
historyClearData()
}

def updated() {
log.info "Life360 User has been Updated"
}

def getDateTime() {
	def date = new Date()
	if(historyHourType == false) state.newdate=date.format("E HH:mm")
	if(historyHourType == true) state.newdate=date.format("E hh:mm a")
}

def historyClearData(){
	if(logEnable) log.debug "Life360 User - clearing the data"
	state.nMessage = "No Data"
	state.s = "s"
	state.speechMap1 = [:]
	state.speechMap1.put(state.s,state.nMessage)
	state.speechMap2 = [:]
	state.speechMap2.put(state.s,state.nMessage)
	state.speechMap3 = [:]
	state.speechMap3.put(state.s,state.nMessage)
	state.speechMap4 = [:]
	state.speechMap4.put(state.s,state.nMessage)
	state.speechMap5 = [:]
	state.speechMap5.put(state.s,state.nMessage)
	state.speechMap6 = [:]
	state.speechMap6.put(state.s,state.nMessage)
	state.speechMap7 = [:]
	state.speechMap7.put(state.s,state.nMessage)
	state.speechMap8 = [:]
	state.speechMap8.put(state.s,state.nMessage)
	state.speechMap9 = [:]
	state.speechMap9.put(state.s,state.nMessage)
	state.speechMap10 = [:]
	state.speechMap10.put(state.s,state.nMessage)
	
	historyLog = "Waiting for Data..."
	sendEvent(name: "history", value: historyLog, displayed: true)
}	

def generatePresenceEvent(boolean present, homeDistance) {
	if(logEnable) log.debug "Life360 generatePresenceEvent (present = $present, homeDistance = $homeDistance)"
	def presence = formatValue(present)
	def linkText = getLinkText(device)
	def descriptionText = formatDescriptionText(linkText, present)
	def handlerName = getState(present)
	def sleeping = (presence == 'not present') ? 'not sleeping' : device.currentValue('sleeping')
	
	if (sleeping != device.currentValue('sleeping')) {
	sendEvent( name: "sleeping", value: sleeping, descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
}
	
def display = presence + (presence == 'present' ? ', ' + sleeping : '')
	if (display != device.currentValue('display')) {
	sendEvent( name: "display", value: display,  )
}
	
	def results = [
		name: "presence",
		value: presence,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
	]
	if(logEnable) log.debug "Generating Event: ${results}"
	sendEvent (results)
	
if(units == "Kilometers" || units == null || units == ""){
	    def statusDistance = homeDistance / 1000
	    def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " km from: Home"
    if(status != device.currentValue('status')){
    sendEvent( name: "status", value: status, isStateChange:true)
    state.update = true}
}else{
	    def statusDistance = (homeDistance / 1000) / 1.609344 
   	    def status = sprintf("%.2f", statusDistance.toDouble().round(2)) + " Miles from: Home"
    if(status != device.currentValue('status')){
   	        sendEvent( name: "status", value: status, isStateChange:true )
        state.update = true
    }
    state.status = status
	}
	
def km = sprintf("%.2f", homeDistance / 1000)
if(km.toDouble().round(2) != device.currentValue('distanceKm')){
sendEvent( name: "distanceKm", value: km.toDouble().round(2) )
state.update = true}

def miles = sprintf("%.2f", (homeDistance / 1000) / 1.609344)
	if(miles.toDouble().round(2) != device.currentValue('distanceMiles')){    
sendEvent( name: "distanceMiles", value: miles.toDouble().round(2) )
	state.update = true}

if(homeDistance.toDouble().round(2) != device.currentValue('distanceMetric')){
	sendEvent( name: "distanceMetric", value: homeDistance.toDouble().round(2) )
	state.update = true}

if(state.update == true){
	sendEvent( name: "lastLocationUpdate", value: "Last location update on:\r\n${formatLocalTime("MM/dd/yyyy @ h:mm:ss a")}" ) 
	state.update = false}
}

private extraInfo(address1,address2,battery,charge,endTimestamp,inTransit,isDriving,latitude,longitude,since,speedMetric,speedMiles,speedKm,wifiState,xplaces,avatar,avatarHtml){
	//if(logEnable) log.debug "extrainfo = Address 1 = $address1 | Address 2 = $address2 | Battery = $battery | Charging = $charge | Last Checkin = $endTimestamp | Moving = $inTransit | Driving = $isDriving | Latitude = $latitude | Longitude = $longitude | Since = $since | Speedmeters = $speedMetric | SpeedMPH = $speedMiles | SpeedKPH = $speedKm | Wifi = $wifiState"
	   
	if(address1 != device.currentValue('address1')){
sendEvent( name: "prevAddress1", value: device.currentValue('address1') )
sendEvent( name: "address1", value: address1, isStateChange: true, displayed: false )
	}
if(address2 != device.currentValue('address2')){
sendEvent( name: "prevAddress2", value: device.currentValue('address2') )
sendEvent( name: "address2", value: address2 )   
	}
	if(battery != device.currentValue('battery'))
   	sendEvent( name: "battery", value: battery )
if(charge != device.currentValue('charge'))
   	sendEvent( name: "charge", value: charge )

def curcheckin = device.currentValue('lastCheckin').toString()
if(endTimestamp != curcheckin)
   	sendEvent( name: "lastCheckin", value: endTimestamp )
if(inTransit != device.currentValue('inTransit'))
   	sendEvent( name: "inTransit", value: inTransit )

	def curDriving = device.currentValue('isDriving')
//if(logEnable) log.debug "Current Driving Status = $curDriving - New Driving Status = $isDriving"
if(isDriving != device.currentValue('isDriving')){
	//if(logEnable) log.debug "If was different, isDriving = $isDriving"
   	sendEvent( name: "isDriving", value: isDriving )
}
def curlat = device.currentValue('latitude').toString()
def curlong = device.currentValue('longitude').toString()
latitude = latitude.toString()
longitude = longitude.toString()
if(latitude != curlat)
sendEvent( name: "latitude", value: latitude )
if(longitude != curlong)
   	sendEvent( name: "longitude", value: longitude )
if(since != device.currentValue('since'))
   	sendEvent( name: "since", value: since )
if(speedMetric != device.currentValue('speedMetric'))
	sendEvent( name: "speedMetric", value: speedMetric )
if(speedMiles != device.currentValue('speedMiles'))
	sendEvent( name: "speedMiles", value: speedMiles )
if(speedKm != device.currentValue('speedKm'))
	sendEvent( name: "speedKm", value: speedKm )
if(wifiState != device.currentValue('wifiState'))
   	sendEvent( name: "wifiState", value: wifiState )
   	setBattery(battery.toInteger(), charge.toBoolean(), charge.toString())
sendEvent( name: "savedPlaces", value: xplaces )
sendEvent( name: "avatar", value: avatar )
sendEvent( name: "avatarHtml", value: avatarHtml )

sendLife360Tile1()
}

def setMemberId (String memberId) {
   if(logEnable) log.debug "MemberId = ${memberId}"
   state.life360MemberId = memberId
}

def getMemberId () {
	if(logEnable) log.debug "MemberId = ${state.life360MemberId}"
return(state.life360MemberId)
}

private String formatValue(boolean present) {
	if (present)
	return "present"
	else
	return "not present"
}

private formatDescriptionText(String linkText, boolean present) {
	if (present)
		return "Life360 User $linkText has arrived"
	else
	return "Life360 User $linkText has left"
}

private getState(boolean present) {
	if (present)
		return "arrived"
	else
	return "left"
}

private toggleSleeping(sleeping = null) {
	sleeping = sleeping ?: (device.currentValue('sleeping') == 'not sleeping' ? 'sleeping' : 'not sleeping')
	def presence = device.currentValue('presence');
	
	if (presence != 'not present') {
		if (sleeping != device.currentValue('sleeping')) {
			sendEvent( name: "sleeping", value: sleeping,  descriptionText: sleeping == 'sleeping' ? 'Sleeping' : 'Awake' )
		}
		
		def display = presence + (presence == 'present' ? ', ' + sleeping : '')
		if (display != device.currentValue('display')) {
			sendEvent( name: "display", value: display )
		}
	}
}

def asleep() {
	toggleSleeping('sleeping')
}

def awake() {
	toggleSleeping('not sleeping')
}

def refresh() {
	parent.refresh()
return null
}

def setBattery(int percent, boolean charging, charge){
	if(percent != device.currentValue("battery"))
		sendEvent(name: "battery", value: percent);
    
def ps = device.currentValue("powerSource") == "BTRY" ? "false" : "true"
if(charge != ps)
		sendEvent(name: "powerSource", value: (charging ? "DC":"BTRY"));
}

private formatLocalTime(format = "EEE, MMM d yyyy @ h:mm:ss a z", time = now()) {
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(time)
}
