package com.example.eivo_androidtv.data.network

import com.example.eivo_androidtv.R
import com.example.eivo_androidtv.domain.model.*

object MockApiService {

    private val regions = listOf("Baleares", "Madrid", "C Valenciana", "Aragón", "Asturias", "Cataluña", "Galicia", "Cantabria")
    private val sports = listOf("Fútbol", "Básquet", "Voleibol", "Fútbol Sala", "Mini Fútbol", "Balonmano", "Tenis", "F1", "MotoGP")
    private val clubList = (1..500).map { i ->
        ClubData(
            id = i.toString(),
            name = when {
                i % 5 == 0 -> "CD UD Poblense $i"
                i % 3 == 0 -> "Atlético $i"
                i % 2 == 0 -> "Real Club $i"
                else -> "Sporting $i"
            },
            logoRes = if (i % 2 == 0) R.drawable.escudo_1 else R.drawable.escudo_2,
            region = regions[i % regions.size]
        )
    }
    private val matchList = (1..1000).map { i ->
        val region = regions[i % regions.size]
        val sport = sports[i % sports.size]
        val isDirecto = i % 7 == 0

        MatchData(
            id = i,
            teamA = "Local $i",
            teamB = "Visitante $i",
            escudoA = if (i % 2 == 0) R.drawable.escudo_1 else R.drawable.escudo_2,
            escudoB = if (i % 3 == 0) R.drawable.escudo_2 else R.drawable.escudo_1,
            category = "Categoría ${1 + (i % 4)}",
            date = if (isDirecto) "EN DIRECTO" else "1${i % 9}:00",
            sport = sport,
            idRegion = region,
            idClub = (1..50).random().toString(),
            isDirecto = isDirecto
        )
    }

    fun getRegions() = regions
    fun getSports() = sports

    fun getDestacados(): List<BannerData> {
        return listOf(
            BannerData(1, "FORMENTERA vs ARTÀ", "Amateur Reg. Pref.", "Dom 2 Nov - 16:55", R.drawable.portero, R.drawable.escudo_1, R.drawable.escudo_2),
            BannerData(2, "BINISSALEM vs POBLENSE", "Tercera División", "Lun 3 Nov - 18:00", R.drawable.portero_2, R.drawable.escudo_1, R.drawable.escudo_2),
            BannerData(3, "MALLORCA vs IBIZA", "Copa Rey", "Mar 4 Nov - 20:00", R.drawable.portero_3, R.drawable.escudo_2, R.drawable.escudo_1)
        )
    }

    fun getAds(): List<AdData> {
        return listOf(
            AdData(101, R.drawable.thumb_ad_1, "home"),
            AdData(102, R.drawable.thumb_ad_2, "home"),
            AdData(103, R.drawable.thumb_ad_1, "pause")
        )
    }

    fun getMatches(
        idComunidad: String,
        idDeporte: String? = null,
        idClub: String? = null,
        fecha: String? = null
    ): List<MatchData> {
        return matchList.filter {
            it.idRegion == idComunidad &&
                    (idDeporte == null || it.sport == idDeporte) &&
                    (idClub == null || it.idClub == idClub) &&
                    !it.isDirecto
        }
    }

    fun getDirectos(idComunidad: String, idDeporte: String? = null): List<MatchData> {
        return matchList.filter {
            it.isDirecto &&
                    it.idRegion == idComunidad &&
                    (idDeporte == null || it.sport == idDeporte)
        }
    }

    fun getClubes(idComunidad: String): List<ClubData> {
        return clubList.filter { it.region == idComunidad }
    }
}