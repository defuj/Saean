package com.saean.app.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object MyFunctions {
    fun checkBoolean(context: Context, target : String) : Boolean{
        val sharedPreferences : SharedPreferences = context.getSharedPreferences(Cache.cacheName,0)
        return sharedPreferences.getBoolean(target,false)
    }

    fun bitmapDescriptorFromVector(context : Context, vectorResId : Int) : BitmapDescriptor {
        val vectorDrawable : Drawable = ContextCompat.getDrawable(context,vectorResId)!!
        vectorDrawable.setBounds(0,0,vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap : Bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun getConnectivityStatus(context: Context) : Boolean{
        var status = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                status = true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                status = true
            }
        } else {
            status = false
        }
        return status
    }

    fun gpsCheck(context: Context) : Boolean{
        val lm : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun gpsPermissionCheck(context: Context) : Boolean{
        val result: Boolean
        val permission :Int = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        result = permission == PackageManager.PERMISSION_GRANTED

        return result
    }

    fun formatDistance(angka : Double) : String{
        val df = DecimalFormat("#.#")
        return df.format(angka)
    }

    fun setLocale(context: Context,lang : String, country : String){
        val locale = Locale(lang,country)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }

    fun loadBitmap(url: String): Bitmap {
        val inputStream: InputStream?
        val bis: BufferedInputStream?
        val conn = URL(url).openConnection()
        conn.connect()
        inputStream = conn.getInputStream()
        bis = BufferedInputStream(inputStream!!, 1024)
        return BitmapFactory.decodeStream(bis)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 75, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    @SuppressLint("SimpleDateFormat")
    fun getJam() : String{
        val DATE_FORMAT_1 = "hh:mm a"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val today : Date = Calendar.getInstance().time
        return dateFormat.format(today).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun milliesToJam(long : Long) : String{
        val DATE_FORMAT_1 = "hh:mm a"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return dateFormat.format(long).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun formatMillie(long : Long, format : String) : String{
        val dateFormat = SimpleDateFormat(format)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return dateFormat.format(long).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun getTanggal(format : String) : String{
        val DATE_FORMAT_1 = format
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val today : Date = Calendar.getInstance().time
        return dateFormat.format(today).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun formatTanggal(tgl : String, formatTo : String,formatFrom : String) : String{
        var tanggal = SimpleDateFormat(formatFrom)
        val newDate: Date = tanggal.parse(tgl)!!
        tanggal = SimpleDateFormat(formatTo)
        return tanggal.format(newDate)
    }

    @SuppressLint("SimpleDateFormat")
    fun dateToMillis(tgl : String,formatFrom : String) : Long{
        val tanggal = SimpleDateFormat(formatFrom)
        val newDate: Date = tanggal.parse(tgl)!!
        return newDate.time
    }

    fun getTime() : Long{
        val today = Date()
        return today.time
    }

    fun getTimeTomorrow() : Long{
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val tomorrow: Date
        tomorrow = calendar.time

        return tomorrow.time
    }

    @SuppressLint("SimpleDateFormat")
    fun getTanggal() : String{
        val DATE_FORMAT_1 = "dd/MM/yy"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val today : Date = Calendar.getInstance().time
        return dateFormat.format(today).toString()
    }

    fun getTanggalApiFormat() : String{
        val DATE_FORMAT_1 = "dd-MM-yyyy"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val today : Date = Calendar.getInstance().time
        return dateFormat.format(today).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun betweenDates(firstDate: Date, secondDate: Date): Long {
        return ChronoUnit.DAYS.between(firstDate.toInstant(), secondDate.toInstant())
    }

    fun base64ToBitmap(decodeImage : String, context: Context) : Drawable{
        val decodedString = Base64.decode(decodeImage, Base64.DEFAULT)
        val decodedByte : Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        return BitmapDrawable(context.resources, decodedByte)
    }

    fun isEmailValid(email : String) : Boolean{
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    /** fun getString(activity: Activity,source : Int) : String{
        val sharedPreferences = activity.getSharedPreferences(Cache.cacheName,0)
        val conf: Configuration = activity.resources.configuration
        conf.locale = Locale(sharedPreferences!!.getString(Cache.language,"")!!,
            sharedPreferences.getString(Cache.country,"")!!)
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val resources = Resources(activity.assets, metrics, conf)

        return resources.getString(source)
    } **/

    fun isSDCardPresent(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun formatUang(harga : Double) : String{
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(harga).replace("Rp","Rp ")
    }

    fun capitalize(capString: String): String {
        val capBuffer = StringBuffer()
        val capMatcher = Pattern.compile(
            "([a-z])([a-z]*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(
                capBuffer,
                capMatcher.group(1)!!.toUpperCase(Locale.getDefault()) + capMatcher.group(2)!!.toLowerCase(Locale.getDefault())
            )
        }
        return capMatcher.appendTail(capBuffer).toString()
    }

    fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // create a matrix for the manipulation
        val matrix = Matrix()
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight)
        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    fun addBorderToBitmap(
        srcBitmap: Bitmap,
        borderWidth: Int,
        borderColor: Int
    ): Bitmap? {
        // Initialize a new Bitmap to make it bordered bitmap
        val dstBitmap = Bitmap.createBitmap(
            srcBitmap.width + borderWidth * 2,  // Width
            srcBitmap.height + borderWidth * 2,  // Height
            Bitmap.Config.ARGB_8888 // Config
        )

        val canvas = Canvas(dstBitmap)

        // Initialize a new Paint instance to draw border
        val paint = Paint()
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true

        val rect = Rect(
            borderWidth / 2,
            borderWidth / 2,
            canvas.width - borderWidth / 2,
            canvas.height - borderWidth / 2
        )

        canvas.drawRect(rect, paint)
        canvas.drawBitmap(srcBitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)
        srcBitmap.recycle()
        return dstBitmap
    }

    fun getCircularBitmap(srcBitmap: Bitmap): Bitmap? {
        // Calculate the circular bitmap width with border
        val squareBitmapWidth = Math.min(srcBitmap.width, srcBitmap.height)

        // Initialize a new instance of Bitmap
        val dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth,  // Width
            squareBitmapWidth,  // Height
            Bitmap.Config.ARGB_8888 // Config
        )

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas to draw circular bitmap
        val canvas = Canvas(dstBitmap)

        // Initialize a new Paint instance
        val paint = Paint()
        paint.isAntiAlias = true

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */
        // Initialize a new Rect instance
        val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)

        /*
            RectF
                RectF holds four float coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be
                accessed directly. Use width() and height() to retrieve the rectangle's width and
                height. Note: most methods do not check to see that the coordinates are sorted
                correctly (i.e. left <= right and top <= bottom).
        */
        // Initialize a new RectF instance
        val rectF = RectF(rect)

        /*
            public void drawOval (RectF oval, Paint paint)
                Draw the specified oval using the specified paint. The oval will be filled or
                framed based on the Style in the paint.

            Parameters
                oval : The rectangle bounds of the oval to be drawn

        */
        // Draw an oval shape on Canvas
        canvas.drawOval(rectF, paint)

        /*
            public Xfermode setXfermode (Xfermode xfermode)
                Set or clear the xfermode object.
                Pass null to clear any previous xfermode. As a convenience, the parameter passed
                is also returned.

            Parameters
                xfermode : May be null. The xfermode to be installed in the paint
            Returns
                xfermode
        */
        /*
            public PorterDuffXfermode (PorterDuff.Mode mode)
                Create an xfermode that uses the specified porter-duff mode.

            Parameters
                mode : The porter-duff mode that is applied

        */paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Calculate the left and top of copied bitmap
        val left = ((squareBitmapWidth - srcBitmap.width) / 2).toFloat()
        val top = ((squareBitmapWidth - srcBitmap.height) / 2).toFloat()

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the

                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */
        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(srcBitmap, left, top, paint)

        // Free the native object associated with this bitmap.
        srcBitmap.recycle()

        // Return the circular bitmap
        return dstBitmap
    }

    fun addBorderToCircularBitmap(
        srcBitmap: Bitmap,
        borderWidth: Int,
        borderColor: Int
    ): Bitmap? {
        // Calculate the circular bitmap width with border
        val dstBitmapWidth = srcBitmap.width + borderWidth * 2

        // Initialize a new Bitmap to make it bordered circular bitmap
        val dstBitmap = Bitmap.createBitmap(dstBitmapWidth, dstBitmapWidth, Bitmap.Config.ARGB_8888)

        // Initialize a new Canvas instance
        val canvas = Canvas(dstBitmap)
        // Draw source bitmap to canvas
        canvas.drawBitmap(srcBitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)

        // Initialize a new Paint instance to draw border
        val paint = Paint()
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true

        /*
            public void drawCircle (float cx, float cy, float radius, Paint paint)
                Draw the specified circle using the specified paint. If radius is <= 0, then nothing
                will be drawn. The circle will be filled or framed based on the Style in the paint.

            Parameters
                cx : The x-coordinate of the center of the cirle to be drawn
                cy : The y-coordinate of the center of the cirle to be drawn
                radius : The radius of the cirle to be drawn
                paint : The paint used to draw the circle
        */
        // Draw the circular border around circular bitmap
        canvas.drawCircle(
            (canvas.width / 2).toFloat(),  // cx
            (canvas.width / 2).toFloat(),  // cy
            (canvas.width / 2 - borderWidth / 2).toFloat(),  // Radius
            paint // Paint
        )

        // Free the native object associated with this bitmap.
        srcBitmap.recycle()

        // Return the bordered circular bitmap
        return dstBitmap
    }

    fun addShadowToCircularBitmap(
        srcBitmap: Bitmap,
        shadowWidth: Int,
        shadowColor: Int
    ): Bitmap? {
        // Calculate the circular bitmap width with shadow
        val dstBitmapWidth = srcBitmap.width + shadowWidth * 2
        val dstBitmap =
            Bitmap.createBitmap(dstBitmapWidth, dstBitmapWidth, Bitmap.Config.ARGB_8888)

        // Initialize a new Canvas instance
        val canvas = Canvas(dstBitmap)
        canvas.drawBitmap(srcBitmap, shadowWidth.toFloat(), shadowWidth.toFloat(), null)

        // Paint to draw circular bitmap shadow
        val paint = Paint()
        paint.color = shadowColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = shadowWidth.toFloat()
        paint.isAntiAlias = true

        // Draw the shadow around circular bitmap
        canvas.drawCircle(
            (dstBitmapWidth / 2).toFloat(),  // cx
            (dstBitmapWidth / 2).toFloat(),  // cy
            (dstBitmapWidth / 2 - shadowWidth / 2).toFloat(),  // Radius
            paint // Paint
        )

        /*
            public void recycle ()
                Free the native object associated with this bitmap, and clear the reference to the
                pixel data. This will not free the pixel data synchronously; it simply allows it to
                be garbage collected if there are no other references. The bitmap is marked as
                "dead", meaning it will throw an exception if getPixels() or setPixels() is called,
                and will draw nothing. This operation cannot be reversed, so it should only be
                called if you are sure there are no further uses for the bitmap. This is an advanced
                call, and normally need not be called, since the normal GC process will free up this
                memory when there are no more references to this bitmap.
        */srcBitmap.recycle()

        // Return the circular bitmap with shadow
        return dstBitmap
    }

    fun dialogMemberExpired(context: Context){
        val dialog = SweetAlertDialog(context,SweetAlertDialog.ERROR_TYPE)
        dialog.titleText = "Masa Berlangganan Berakhir"
        dialog.contentText = "Silahkan untuk memperpanjang masa berlangganan VIP/VVIP"
        dialog.show()
    }

    fun dialogOnTaaruf(context: Context){
        val dialog = SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
        dialog.titleText = "Perhatian"
        dialog.contentText = "Anda sedang dalam masa taaruf/khitbah."
        dialog.show()
    }

    fun dialogAlreadyMarried(context: Context){
        val dialog = SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
        dialog.titleText = "Oops"
        dialog.contentText = "Anda sudah menikah."
        dialog.show()
    }

    fun dialogOnFree(context: Context){
        val dialog = SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE)
        dialog.titleText = "Perhatian"
        dialog.contentText = "Fitur ini hanya tersedia bagi member VIP/VVIP"
        dialog.show()
    }

    fun randomAlphaNumeric(count: Int): String? {
        val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var counts = count
        val builder = StringBuilder()
        while (counts-- != 0) {
            val character = (Math.random() * ALPHA_NUMERIC_STRING.length).toInt()
            builder.append(ALPHA_NUMERIC_STRING[character])
        }
        return builder.toString()
    }

    fun stopScreenshot(activity: Activity){
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun encrypt(password : String): String {
        val hexChars = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz="
        val bytes = MessageDigest
            .getInstance("SHA-256") //SHA-512
            .digest(password.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }

    fun changeToUnderscore(content : String) : String{
        val email = content.replace(".","_")
        return email.replace("-","_")
    }

    fun closeKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}