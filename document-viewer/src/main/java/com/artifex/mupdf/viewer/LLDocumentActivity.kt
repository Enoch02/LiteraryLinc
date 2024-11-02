package com.artifex.mupdf.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.viewer.model.ContentState
import com.artifex.mupdf.viewer.model.SearchResult
import com.artifex.mupdf.viewer.ui.ReaderBottomBar
import com.artifex.mupdf.viewer.ui.ReaderTopBar
import com.artifex.mupdf.viewer.shared.Item
import com.artifex.mupdf.viewer.ui.ReaderView
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.resources.theme.LiteraryLincTheme
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LLDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        setContent {
            val viewModel: LLReaderViewModel = hiltViewModel()
            val dynamicColor by viewModel.getBooleanPreference(key = SettingsRepository.PreferenceType.DYNAMIC_COLOR)
                .collectAsState(initial = null)

            if (Intent.ACTION_VIEW == intent.action) {
                val uri = intent.data
                val mimeType = getIntent().type
                val documentId = intent.getStringExtra("id")

                LiteraryLincTheme(
                    alwaysDark = true,
                    dynamicColor = dynamicColor ?: false,
                    content = {
                        if (uri == null) {
                            Text("Cannot open Document")
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    ReaderView(
                                        uri = uri,
                                        mimeType = mimeType,
                                        documentId = documentId,
                                        viewModel = viewModel
                                    )
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
