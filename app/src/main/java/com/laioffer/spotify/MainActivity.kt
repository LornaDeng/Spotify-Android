package com.laioffer.spotify

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.laioffer.spotify.database.DatabaseDao
import com.laioffer.spotify.datamodel.Album
import com.laioffer.spotify.ui.theme.SpotifyTheme
import android.util.Log
import com.laioffer.spotify.network.NetworkApi
import com.laioffer.spotify.player.PlayerBar
import com.laioffer.spotify.player.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// customized extend AppCompatActivity
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val playerViewModel: PlayerViewModel by viewModels()

    @Inject
    lateinit var api: NetworkApi

    @Inject
    lateinit var databaseDao: DatabaseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* setContent {
            SpotifyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AlbumCover()
                }
            }
        } */
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph)

        NavigationUI.setupWithNavController(navView, navController)
        navView.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            navController.popBackStack(item.itemId, inclusive = false)
            true
        }

        findViewById<ComposeView>(R.id.player_bar).setContent {
            MaterialTheme(colors = darkColors()) {
                PlayerBar(viewModel = playerViewModel)
            }
        }

        // Test retrofit
        lifecycleScope.launch(Dispatchers.IO) {
            val response = api.getHomeFeed().execute().body()
            Log.d("Network", response.toString())
        }

        // Room integration example. This runs every time the activity is created.
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                databaseDao.favoriteAlbum(
                    Album(
                        id = 1,
                        name = "Hexagonal",
                        year = "2008",
                        cover = "https://upload.wikimedia.org/wikipedia/en/6/6d/Leessang-Hexagonal_%28cover%29.jpg",
                        artists = "Leessang",
                        description = "Leessang was a South Korean hip hop duo composed of Gary and Gil."
                    )
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true, widthDp = 412, heightDp = 732)
@Composable
fun DefaultPreview() {
    SpotifyTheme {
        Surface {
            AlbumCover()
        }
    }
}

@Composable
private fun LoadingSection(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = Color.White
        )
    }
}

@Composable
private fun AlbumCover() {
    Column {
        Box(modifier = Modifier.size(160.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://upload.wikimedia.org/wikipedia/en/d/d1/Stillfantasy.jpg")
                    .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Android Emulator) Chrome/120.0"
                    )
                    .build(),
                contentDescription = "Still Fantasy album cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = "Still Fantasy",
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 4.dp, start = 2.dp)
                    .align(Alignment.BottomStart)
            )
        }

        Text(
            text = "Jay Chou",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            color = Color.LightGray
        )
    }
}
