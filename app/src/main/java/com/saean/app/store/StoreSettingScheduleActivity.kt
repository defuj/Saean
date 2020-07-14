 package com.saean.app.store

import android.R
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saean.app.helper.Cache
import kotlinx.android.synthetic.main.activity_store_setting_schedule.*


 class StoreSettingScheduleActivity : AppCompatActivity() {
     private lateinit var database: FirebaseDatabase
     private var sharedPreferences : SharedPreferences? = null
     private lateinit var storage : FirebaseStorage
     private lateinit var storageReference : StorageReference
     private var opened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.saean.app.R.layout.activity_store_setting_schedule)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        sharedPreferences =getSharedPreferences(Cache.cacheName,0)

        setupFunctions()
    }

     private fun setupFunctions() {
         toolbarSchedule.setNavigationOnClickListener { finish() }

         setupDays()
     }

     @SuppressLint("SetTextI18n")
     private fun setupDays() {
         val storeID = sharedPreferences!!.getString(Cache.storeID,"")
         val schedule = database.getReference("store/$storeID/storeSchedule")
         schedule.addListenerForSingleValueEvent(object : ValueEventListener{
             override fun onCancelled(error: DatabaseError) {

             }

             override fun onDataChange(snapshot: DataSnapshot) {
                 if(snapshot.exists()){
                     for (days in snapshot.children){
                         when (days.key) {
                             "Minggu" -> {
                                 day1.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner1start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner1start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner1end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner1end.setSelection(spinnerPosition)
                                 }
                             }
                             "Senin" -> {
                                 day2.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner2start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner2start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner2end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner2end.setSelection(spinnerPosition)
                                 }
                             }
                             "Selasa" -> {
                                 day3.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner3start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner3start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner3end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner3end.setSelection(spinnerPosition)
                                 }
                             }
                             "Rabu" -> {
                                 day4.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner4start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner4start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner4end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner4end.setSelection(spinnerPosition)
                                 }
                             }
                             "Kamis" -> {
                                 day5.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner5start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner5start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner5end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner5end.setSelection(spinnerPosition)
                                 }
                             }
                             "Jumat" -> {
                                 day6.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner6start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner6start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner6end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner6end.setSelection(spinnerPosition)
                                 }
                             }
                             "Sabtu" -> {
                                 day7.isChecked = days.child("status").getValue(Boolean::class.java)!!
                                 if(days.child("startOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("startOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner7start.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner7start.setSelection(spinnerPosition)
                                 }

                                 if(days.child("endOpen").getValue(String::class.java)!!.isNotEmpty()){
                                     val compareValue = days.child("endOpen").getValue(String::class.java)!!
                                     val adapter = ArrayAdapter.createFromResource(this@StoreSettingScheduleActivity, com.saean.app.R.array.jam, R.layout.simple_spinner_item)
                                     adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                     spinner7end.adapter = adapter
                                     val spinnerPosition = adapter.getPosition(compareValue)
                                     spinner7end.setSelection(spinnerPosition)
                                 }
                             }
                         }
                     }
                 }else{
                     schedule.child("Minggu").child("status").setValue(false)
                     schedule.child("Minggu").child("startOpen").setValue("00.00")
                     schedule.child("Minggu").child("endOpen").setValue("00.00")

                     schedule.child("Senin").child("status").setValue(false)
                     schedule.child("Senin").child("startOpen").setValue("00.00")
                     schedule.child("Senin").child("endOpen").setValue("00.00")

                     schedule.child("Selasa").child("status").setValue(false)
                     schedule.child("Selasa").child("startOpen").setValue("00.00")
                     schedule.child("Selasa").child("endOpen").setValue("00.00")

                     schedule.child("Rabu").child("status").setValue(false)
                     schedule.child("Rabu").child("startOpen").setValue("00.00")
                     schedule.child("Rabu").child("endOpen").setValue("00.00")

                     schedule.child("Kamis").child("status").setValue(false)
                     schedule.child("Kamis").child("startOpen").setValue("00.00")
                     schedule.child("Kamis").child("endOpen").setValue("00.00")

                     schedule.child("Jumat").child("status").setValue(false)
                     schedule.child("Jumat").child("startOpen").setValue("00.00")
                     schedule.child("Jumat").child("endOpen").setValue("00.00")

                     schedule.child("Sabtu").child("status").setValue(false)
                     schedule.child("Sabtu").child("startOpen").setValue("00.00")
                     schedule.child("Sabtu").child("endOpen").setValue("00.00")
                 }
             }
         })

         day1.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day1.text = "Buka"
                 schedule.child("Minggu").child("status").setValue(true)
             }else{
                 day1.text = "Tutup"
                 schedule.child("Minggu").child("status").setValue(false)
             }
         }

         day2.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day2.text = "Buka"
                 schedule.child("Senin").child("status").setValue(true)
             }else{
                 day2.text = "Tutup"
                 schedule.child("Senin").child("status").setValue(false)
             }
         }

         day3.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day3.text = "Buka"
                 schedule.child("Selasa").child("status").setValue(true)
             }else{
                 day3.text = "Tutup"
                 schedule.child("Selasa").child("status").setValue(false)
             }
         }

         day4.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day4.text = "Buka"
                 schedule.child("Rabu").child("status").setValue(true)
             }else{
                 day4.text = "Tutup"
                 schedule.child("Rabu").child("status").setValue(false)
             }
         }

         day5.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day5.text = "Buka"
                 schedule.child("Kamis").child("status").setValue(true)
             }else{
                 day5.text = "Tutup"
                 schedule.child("Kamis").child("status").setValue(false)
             }
         }

         day6.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day6.text = "Buka"
                 schedule.child("Jumat").child("status").setValue(true)
             }else{
                 day6.text = "Tutup"
                 schedule.child("Jumat").child("status").setValue(false)
             }
         }

         day7.setOnCheckedChangeListener { _, isChecked ->
             if(isChecked){
                 day7.text = "Buka"
                 schedule.child("Sabtu").child("status").setValue(true)
             }else{
                 day7.text = "Tutup"
                 schedule.child("Sabtu").child("status").setValue(false)
             }
         }

         spinner1start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Minggu").child("startOpen").setValue(spinner1start.selectedItem.toString())
                 }
             }
         }
         spinner1end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Minggu").child("endOpen").setValue(spinner1end.selectedItem.toString())
                 }
             }
         }

         spinner2start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Senin").child("startOpen").setValue(spinner2start.selectedItem.toString())
                 }
             }
         }
         spinner2end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Senin").child("endOpen").setValue(spinner2end.selectedItem.toString())
                 }
             }
         }

         spinner3start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Selasa").child("startOpen").setValue(spinner3start.selectedItem.toString())
                 }
             }
         }
         spinner3end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Selasa").child("endOpen").setValue(spinner3end.selectedItem.toString())
                 }
             }
         }

         spinner4start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Rabu").child("startOpen").setValue(spinner4start.selectedItem.toString())
                 }
             }
         }
         spinner4end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Rabu").child("endOpen").setValue(spinner4end.selectedItem.toString())
                 }
             }
         }

         spinner5start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Kamis").child("startOpen").setValue(spinner5start.selectedItem.toString())
                 }
             }
         }
         spinner5end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Kamis").child("endOpen").setValue(spinner5end.selectedItem.toString())
                 }
             }
         }

         spinner6start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Jumat").child("startOpen").setValue(spinner6start.selectedItem.toString())
                 }
             }
         }
         spinner6end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Jumat").child("endOpen").setValue(spinner6end.selectedItem.toString())
                 }
             }
         }
         spinner7start.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Sabtu").child("startOpen").setValue(spinner7start.selectedItem.toString())
                 }
             }
         }
         spinner7end.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onNothingSelected(parent: AdapterView<*>?) {

             }

             override fun onItemSelected(
                 parent: AdapterView<*>?,
                 view: View?,
                 position: Int,
                 id: Long) {
                 if(opened){
                     schedule.child("Sabtu").child("endOpen").setValue(spinner7end.selectedItem.toString())
                 }
             }
         }

         opened = true
     }
 }