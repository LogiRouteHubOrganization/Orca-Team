package domain.algorithm.geoHash

private const val BASE_32 = "0123456789bcdefghjkmnpqrstuvwxyz"
fun encodeGeoHash (
    Longitude: Double,
    Latitude: Double,
    Precision : Int =12
):String{
    require(Latitude in -90.0..90.0  ){"Latitude  must between -90 and 90"}
    require(Longitude in -180.0..180.0){"Longitude  must between -180 and 180"}
    require(Precision in 1..12){"Precision must between 1 and 12 "}
    val geohashBits=StringBuilder()
    var maxLon=180
    var minLon=-180
    var maxLat=90
    var minLat=-90
    var isLongitude=true
    var bitCount=0
    var charIndex = 0
    while(geohashBits.length<Precision){
        if(isLongitude){
            var mid=(maxLon+minLon)/2
            if(Longitude >= mid){
                charIndex = (charIndex shl 1) or 1
                minLon=mid
            }
            else {
                charIndex = charIndex shl 1
                maxLon = mid
            }
        }
        else{
            var mid=(maxLat+minLat)/2
            if(Latitude >= mid){
                charIndex = (charIndex shl 1) or 1
                minLat=mid
            }
            else{
                charIndex = charIndex shl 1
                maxLat = mid
            }
        }
        isLongitude = !isLongitude
        bitCount++
        if (bitCount == 5) {
            geohashBits.append(BASE_32[charIndex])
            bitCount=0
            charIndex = 0
        }

    }
    return geohashBits.toString()

}