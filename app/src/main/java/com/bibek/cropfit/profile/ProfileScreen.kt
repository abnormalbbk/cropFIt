package com.bibek.cropfit.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bibek.cropfit.MainActivity
import com.bibek.cropfit.R
import com.bibek.cropfit.components.PatternedColumn
import com.bibek.cropfit.login.ui.theme.Green
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen() {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.hello))
    val progress = animateLottieCompositionAsState(
        composition.value, iterations = LottieConstants.IterateForever, speed = 1.5f
    )
    val lottieAnimatable = rememberLottieAnimatable()

    LaunchedEffect(Unit) {
        lottieAnimatable.animate(
            composition.value,
            iterations = LottieConstants.IterateForever,
            clipSpec = LottieClipSpec.Progress(0.5f, 0.75f),
        )
    }

    PatternedColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(200.dp)
                .align(alignment = Alignment.CenterHorizontally),
            composition = composition.value,
            progress = { progress.value },
        )
        Text(
            buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.headlineMedium.toSpanStyle()) {
//                    append("Bibek Maharjan")
                    append(user?.displayName)
                }
                withStyle(style = MaterialTheme.typography.headlineSmall.toSpanStyle()) {
                    append("\n\nAre you enjoying")
                }
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Row(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
            Image(painter = painterResource(R.drawable.ic_logo), contentDescription = "Logo")
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.headlineMedium.toSpanStyle()
                            .copy(color = Green)
                    ) {
                        append("CropFit")
                    }
                    withStyle(
                        style = MaterialTheme.typography.headlineLarge.toSpanStyle()
                            .copy(color = Green)
                    ) {
                        append("?")
                    }
                }, modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
        }

        Row(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(color = Color.Black)
                    ) {
                        append("To rate us, Tap here")
                    }
                }, modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
            IconButton(
                modifier = Modifier.size(64.dp), onClick = {
                    openPlayStore(context, "com.bibek.cropFit")
                }) {
                Image(
                    modifier = Modifier.padding(8.dp),
                    painter = painterResource(R.drawable.ic_google_play),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Email, contentDescription = "Email", tint = Green

            )
            Text(
                user?.email ?: "",
//                "abnormal.bbk@gmail.com",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(alignment = Alignment.CenterVertically)
            )
        }

        Text(
            "Not this user?",
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
        )
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(
                    context, MainActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)

            }, modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text("Sign Out")
        }
    }
}


fun openPlayStore(context: Context, packageName: String) {
    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // fallback to browser
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {

    ProfileScreen()
}