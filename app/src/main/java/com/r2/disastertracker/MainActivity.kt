package com.r2.disastertracker


import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.r2.disastertracker.adapter.DisasterAdapter
import com.r2.disastertracker.api.ApiClient
import com.r2.disastertracker.data.DisasterResponses
import com.r2.disastertracker.data.Geometry
import com.r2.disastertracker.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sheet: BottomSheetBehavior<View>
    private lateinit var disasterList : ArrayList<Geometry>
    private lateinit var adapter: DisasterAdapter
    private lateinit var searchView: SearchView
    private lateinit var mapView: MapView
    private lateinit var mapControllerMain: MapController
    var modeTheme: Boolean = true
    private lateinit var startString: String
    private lateinit var endString: String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      Loading screen
        Thread.sleep(1000)
        installSplashScreen()

        disasterList = arrayListOf<Geometry>()
        binding = ActivityMainBinding.inflate(layoutInflater)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(binding.root)



//      Recyclerview
        adapter = DisasterAdapter(this@MainActivity, arrayListOf())
        binding.rvMain.adapter = adapter
        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.setHasFixedSize(true)

//        BottomSHEET
        sheet = BottomSheetBehavior.from(binding.mainActivity)
        sheet.apply {
            peekHeight = 400
            isHideable = false
            isDraggable= true
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

//        SET MAPS
        mapView = binding.mapViewmain
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapControllerMain = mapView.controller as MapController
        mapControllerMain.zoomTo(6.5)
        mapView.controller.animateTo(GeoPoint(-6.9815720547, 110.3182915623))
        mapView.setMultiTouchControls(true)

//      Dark/Light Mode
        val swtch = binding.swtch
        swtch.setOnClickListener{
            if(modeTheme){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                modeTheme = false
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                modeTheme = true
            }

        }


//        GET API
        remoteData()
        binding.btn.setOnClickListener {
            remoteData()

        }
        callArchiveDialog()

//        SearchView
        searchView = binding.sVMain
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
             override fun onQueryTextSubmit(newText: String?): Boolean {

                 return false
             }

             override fun onQueryTextChange(newText: String?): Boolean {
                 filterList(newText)
                 return true
             }

         })
        mapView.invalidate()

    }




    fun remoteData(){
        ApiClient.apiService.getReport("604800").enqueue(object: Callback<DisasterResponses>{
            override fun onResponse(
                call: Call<DisasterResponses>,
                response: Response<DisasterResponses>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()?.result?.objects?.output?.geometries
                    disasterList = setDataToAdapter(body!!)
                    getMap(body)

                }
            }

            override fun onFailure(call: Call<DisasterResponses>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Server Error", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun remoteDataArchive(start: String, end:String){
        Log.d("ISISTAR","${start}T00:00:00+0700")
        ApiClient.apiService.getArchive("${start}T00:00:00+0700", "${end}T23:59:59+0700").enqueue(object: Callback<DisasterResponses>{
            override fun onResponse(
                call: Call<DisasterResponses>,
                response: Response<DisasterResponses>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()?.result?.objects?.output?.geometries
                    disasterList = setDataToAdapter(body!!)
                    getMap(body)
                }
            }

            override fun onFailure(call: Call<DisasterResponses>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Server Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setDataToAdapter(data: ArrayList<Geometry>): ArrayList<Geometry>{
        adapter.setData(data)
        val data1 = data
        return data1
    }

    fun filterList(query: String?){
        if (query != null) {
            val filteredList = ArrayList<Geometry>()
            mapView.overlays.clear()
            mapView.invalidate()
            for (i in disasterList) {
                if (
                    i.properties.disasterType.lowercase(Locale.ROOT).contains(query)
//                    || i.properties.created.lowercase(Locale.ROOT).contains(query)
                    || i.properties.tags.region?.lowercase()?.contains(query) ?: true
                    || i.properties.tags.region?.contains(query) ?: true
                ) {
                    filteredList.add(i)


                }
            }
            Log.d("ISI Filter List", "$filteredList")
            if (filteredList.isEmpty()) {
                Log.d("DATAMAP","EMPTY")
            } else{
                adapter.setFilteredList(filteredList)
                getMap(filteredList)


            }
        }

    }


    fun getMap(data: ArrayList<Geometry>){
        mapView.overlays.clear()
        mapView.invalidate()
        for (i in 0 until data.size){
            var geoPoint = GeoPoint(
                getMyDoubleValue(data[i].coordinates[1]),
                getMyDoubleValue(data[i].coordinates[0])
            )

            val marker = Marker(mapView)
            marker.icon = getMarkerIcon(data[i].properties.disasterType.lowercase(Locale.ROOT))
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP)
            mapView.overlays.add(marker)

        }
        mapView.invalidate()
    }



    fun getMarkerIcon(disaster: String): Drawable?{
        var icon: Drawable?
        when(disaster){
            "flood" -> icon = resources.getDrawable(R.drawable.baseline_water_drop_24)
            "volcano" -> icon = resources.getDrawable(R.drawable.baseline_volcano_24)
            "wind" ->  icon = resources.getDrawable(R.drawable.baseline_air_24)
            "haze" -> icon = resources.getDrawable(R.drawable.baseline_dehaze_24)
            "fire" ->  icon= resources.getDrawable(R.drawable.baseline_local_fire_department_24)
            "earthquake" -> icon = resources.getDrawable(R.drawable.baseline_broken_image_24)
            else ->  icon = resources.getDrawable(R.drawable.baseline_place_24)

        }
        return icon
    }


    fun getMyDoubleValue(vararg any: Any) : Double {
        return when(val tmp = any.first()) {
            is Number -> tmp.toDouble()
            else -> throw Exception("not a number") // or do something else reasonable for your case
        }
    }

    fun callArchiveDialog(){

        val button : Button = findViewById(R.id.btn1)
        button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.archive_set_date, null)
            val startDate = dialogLayout.findViewById<EditText>(R.id.startDate)
            val endDate = dialogLayout.findViewById<EditText>(R.id.endDate)

            with(builder){
                setTitle("Pilih Tanggal Arsip")
                setPositiveButton("Ok"){dialog, which ->
                    startString = startDate.text.toString()
                    endString = endDate.text.toString()
                    remoteDataArchive(startString, endString)
                }
                setNegativeButton("Cancel"){dialog, which ->
                }
                setView(dialogLayout)
                show()
            }


        }
    }

}