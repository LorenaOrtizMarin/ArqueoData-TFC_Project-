package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.lorenaortiz.arqueodata.R
import com.lorenaortiz.arqueodata.ui.theme.MainText

@Composable
fun IntroScreen(navController: NavController) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val (image, title, subtitle) = createRefs()

        Image(
            painter = painterResource(R.drawable.ruinas),
            contentDescription = "ruinasIntro",
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top, margin = 60.dp)
                    bottom.linkTo(title.top, margin = 0.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.matchParent
                    height = Dimension.percent(0.6f)
                }
        )

        Text(
            text = "ArqueoData",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MainText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(image.bottom, margin = 1.dp)
                    bottom.linkTo(subtitle.top, margin = 0.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.matchParent
                }
        )

        Text(
            text = "Comienza a gestionar tu yacimiento arqueológico",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MainText,
            modifier = Modifier
                .constrainAs(subtitle) {
                    top.linkTo(title.bottom, margin = 25.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.value(300.dp)
                }
        )
    }
}

/*@Preview(showBackground = true)
@Composable
fun PreviewIntroScreen(){
    IntroScreen(navController = {})
}
*/