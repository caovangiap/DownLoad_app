package com.example

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiServiceDowwnLoadFile {

    @Streaming
    @GET("/videoplayback?expire=1670948624&ei=sFKYY5jACs6nwgPwlo3YCA&ip=2402%3A800%3A61b1%3A3040%3A1043%3A6837%3A2548%3A514a&id=o-ADXmpP0kGBB1JyGNTEJxaV3qNR7PP9CfGstU4-NqbGPb&itag=22&source=youtube&requiressl=yes&mh=DG&mm=31%2C29&mn=sn-8pxuuxa-i5od6%2Csn-8pxuuxa-i5ozz&ms=au%2Crdu&mv=m&mvi=1&pl=50&initcwndbps=2122500&spc=SFxXNv26pvTU_zER3LoF8lqOwxi3n5E&vprv=1&svpuc=1&mime=video%2Fmp4&cnr=14&ratebypass=yes&dur=969.200&lmt=1635161271377315&mt=1670926637&fvip=1&fexp=24001373%2C24007246&c=ANDROID&txp=5532432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRAIgG-OPyPALG1QRvEl8taWzsenqhN-QP15vLOC32o7eViICIHfikHBkMg33CPGhFoZJYfklX57yTo5qtEms8X2ojqyx&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhAJdfWPZV8oHjkbt2fgqEH684HfMcpIpUG-jMJUYGbfzOAiAFkwmz7Mf-AWqcnw8-cqNhA1UV2nRkz0LkvEn8Z2wHoQ%3D%3D&cpn=3Tp6xlTqksZ1WRQH")
    fun downloadFileWithFixedUrl(): Call<ResponseBody>

    @Streaming
    @GET()
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String): Call<ResponseBody>
}