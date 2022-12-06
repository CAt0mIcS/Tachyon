package com.tachyonmusic.user.data.repository

import com.tachyonmusic.domain.repository.FileRepository
import java.io.File


class TestFileRepositoryImpl : FileRepository {
    init {
//        _songs.value = listOf<Song>(
//
//            LocalSongImpl(
//                MediaId("*0*/Music/Assasin's Creed Trailer Music - Machinima.mp3"),
//                "Assasin's Creed Trailer Music - Machinima",
//                "Unknown Artist",
//                320376L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Awake - Nok From the Future.mp3"),
//                "Awake",
//                "Nok From the Future",
//                175200L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Bone Dry - Tristam.mp3"), "Bone Dry", "Tristam", 276360L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Bring Me To Life - Nightcore.mp3"),
//                "Bring Me To Life",
//                "Nightcore",
//                198480L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Brooklyn - Glockenbach.mp3"),
//                "Brooklyn",
//                "Glockenbach",
//                185808L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Burn It All Down - League of Legends.mp3"),
//                "Burn It All Down",
//                "League of Legends",
//                200280L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Bush Week - Nihilore.mp3"),
//                "Bush Week",
//                "Nihilore",
//                353064L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Cinematic Hybrid Trailer - Tybercore.mp3"),
//                "Cinematic Hybrid Trailer",
//                "Tybercore",
//                146592L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Britney Spears - Circus.mp3"),
//                "Circus",
//                "Britney Spears",
//                192552L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/A Himitsu - Cosmic Storm.mp3"),
//                "Cosmic Storm",
//                "A Himitsu",
//                244898L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Cymatics - Nigel Stanford.mp3"),
//                "Cymatics",
//                "Nigel Stanford",
//                266376L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/DANGER - 0.59 - MrSuicideSheep.mp3"),
//                "DANGER - 0:59",
//                "MrSuicideSheep",
//                254352L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Don't Play - JAEGER.mp3"),
//                "Don't Play",
//                "JAEGER",
//                196571L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Epic Suspense Trailer Music - AndrewG.mp3"),
//                "Epic Suspense Trailer Music",
//                "AndrewG",
//                135504L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Est-ce que tu m'aimes - Maître Gims.mp3"),
//                "Est-ce que tu m'aimes",
//                "Maître Gims",
//                233928L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Fading Love - Day 7.mp3"),
//                "Fading Love",
//                "Day 7",
//                185304L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Far Centaurus - Nigel Stanford.mp3"),
//                "Far Centaurus",
//                "Nigel Stanford",
//                113088L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Final Frontier - Thomas Bergersen.mp3"),
//                "Final Frontier",
//                "Thomas Bergersen",
//                279768L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Simon Curtis - Flesh.mp3"),
//                "Flesh",
//                "Simon Curtis",
//                263136L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Fruit Punch - Rhythm Ninja.mp3"),
//                "Fruit Punch",
//                "Rhythm Ninja",
//                113640L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Glow (Vokon Remix) - Voyage.mp3"),
//                "Glow (Vokon Remix)",
//                "Voyage",
//                252072L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Guardian Music.mp3"),
//                "Guardian Music",
//                "Unknown Artist",
//                794504L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Heaven and Hell - Jeremy Blake.mp3"),
//                "Heaven and Hell",
//                "Jeremy Blake",
//                262824L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Icarus - Ivan Torrent.mp3"),
//                "Icarus",
//                "Ivan Torrent",
//                272880L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/In the Dark - Sophie and the Giants.mp3"),
//                "In the Dark",
//                "Sophie and the Giants",
//                185472L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Last Friday Night - Katy Perry.mp3"),
//                "Last Friday Night",
//                "Katy Perry",
//                226800L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Last Time - Nerxa.mp3"), "Last Time", "Nerxa", 127032L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Legends Never Die.mp3"),
//                "Legends Never Die",
//                "Pentakill",
//                234083L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/melodysheep - Life Beyond 3 Trailer Music.mp3"),
//                "Life Beyond 3 Trailer Music",
//                "melodysheep",
//                82800L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Luh Me Like That - Leeyou & Danceey.mp3"),
//                "Luh Me Like That",
//                "Leeyou & Danceey",
//                211872L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Magic of Hong Kong.mp3"),
//                "Magic of Hong Kong",
//                "Artem Zinovyev",
//                204330L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Match In The Rain - Alec Benjamin.mp3"),
//                "Match In The Rain",
//                "Alec Benjamin",
//                156744L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Melody - Sigala.mp3"), "Melody", "Sigala", 166848L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Middle of the Night - Elley Duhé.mp3"),
//                "Middle of the Night",
//                "Elley Duhé",
//                182400L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Moonriser - Ivan Torrent.mp3"),
//                "Moonriser",
//                "Ivan Torrent",
//                214488L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Nevada - Vicetone.mp3"), "Nevada", "Vicetone", 204648L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Two Steps From Hell - None Shall Live.mp3"),
//                "None Shall Live",
//                "Two Steps From Hell",
//                377496L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/OUT OUT - Joel Corry x Jax Jones.mp3"),
//                "OUT OUT",
//                "Joel Corry x Jax Jones",
//                159216L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Our Last Stand - Niklas Johansson.mp3"),
//                "Our Last Stand",
//                "Niklas Johansson",
//                164880L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Over Endless Fields - David Celeste.mp3"),
//                "Over Endless Fields",
//                "David Celeste",
//                165240L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Paradox (Intro) - Chris Potts.mp3"),
//                "Paradox (Intro)",
//                "Chris Potts",
//                162840L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Miley Cyrus - Prisoner.mp3"),
//                "Prisoner",
//                "Miley Cyrus",
//                167088L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Rain In Ibiza - Felix Jaehn.mp3"),
//                "Rain In Ibiza",
//                "Felix Jaehn",
//                141312L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Rebirth - RVRSPLAY.mp3"), "Rebirth", "RVRSPLAY", 161016L),
//            LocalSongImpl(MediaId("*0*/Music/Remedy - Leony.mp3"), "Remedy", "Leony", 142968L),
//            LocalSongImpl(MediaId("*0*/Music/She's Back - Arcane.mp3"), "She's Back", "Arcane", 66691L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Starship Super Heavy Testing This Month - spaceXcentric.mp3"),
//                "Starship Super Heavy Testing This Month",
//                "spaceXcentric",
//                10848L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Sun Mother - melodysheep.mp3"),
//                "Sun Mother",
//                "melodysheep",
//                216480L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Supernatural - Kevin MacLeod.mp3"),
//                "Supernatural",
//                "Kevin MacLeod",
//                49464L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Take Control Space - cleanmindsounds.mp3"),
//                "Take Control Space",
//                "cleanmindsounds",
//                88104L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Taking Flight - Greg Dombrowski.mp3"),
//                "Taking Flight",
//                "Greg Dombrowski",
//                184776L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Talon - Overwatch League Music.mp3"),
//                "Talon",
//                "Overwatch League Music",
//                143544L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Terminant - Nihilore.mp3"),
//                "Terminant",
//                "Nihilore",
//                390534L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/The Business - Tiesto.mp3"),
//                "The Business",
//                "Tiesto",
//                162384L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/The Vibe - MARSHVLL.mp3"),
//                "The Vibe",
//                "MARSHVLL",
//                189360L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Transitions - Cotton Niblett.mp3"),
//                "Transitions",
//                "Cotton Niblett",
//                211128L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Tomsize - Trap Life.mp3"),
//                "Trap Life",
//                "Tomsize",
//                228441L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/United Through Fire - Dual Motion Music.mp3"),
//                "United Through Fire",
//                "Dual Motion Music",
//                188904L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Unleash My Halo.mp3"),
//                "Unleash My Halo",
//                "Sum Wave",
//                109440L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Violet Burn - Greg Dombrowski.mp3"),
//                "Violet Burn",
//                "Greg Dombrowski",
//                166176L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Walk - Kwabs.mp3"), "Walk", "Kwabs", 211848L),
//            LocalSongImpl(
//                MediaId("*0*/Music/Welcome to the Playground - Bea Miller.mp3"),
//                "Welcome to the Playground",
//                "Bea Miller",
//                226992L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/West Coast - OneRepublic.mp3"),
//                "West Coast",
//                "OneRepublic",
//                191712L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/What Could Have Been - Sting.mp3"),
//                "What Could Have Been",
//                "Sting",
//                208968L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/Where I'll Be Waiting - Rich Edwards.mp3"),
//                "Where I'll Be Waiting",
//                "Rich Edwards",
//                191352L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/ABT X Topic X A7S - Your Love.mp3"),
//                "Your Love",
//                "ABT X Topic X A7S",
//                147912L
//            ),
//            LocalSongImpl(
//                MediaId("*0*/Music/closer - n u a g e s.mp3"),
//                "closer",
//                "n u a g e s",
//                302928L
//            ),
//            LocalSongImpl(MediaId("*0*/Music/Isolated - Ora.mp3"), "Òra", "Isolated", 208248L),
//
//            ).sortedBy { it.title + it.artist }
    }

    override fun getFilesInDirectoryWithExtensions(
        directory: File,
        extensions: List<String>
    ): List<File> {
        return listOf<File>(
            File("/storage/emulated/0/Music/Assasin's Creed Trailer Music - Machinima.mp3"),
            File("/storage/emulated/0/Music/Awake - Nok From the Future.mp3"),
            File("/storage/emulated/0/Music/Bone Dry - Tristam.mp3"),
            File("/storage/emulated/0/Music/Bring Me To Life - Nightcore.mp3"),
            File("/storage/emulated/0/Music/Brooklyn - Glockenbach.mp3"),
            File("/storage/emulated/0/Music/Burn It All Down - League of Legends.mp3"),
            File("/storage/emulated/0/Music/Bush Week - Nihilore.mp3"),
            File("/storage/emulated/0/Music/Cinematic Hybrid Trailer - Tybercore.mp3"),
            File("/storage/emulated/0/Music/Britney Spears - Circus.mp3"),
            File("/storage/emulated/0/Music/A Himitsu - Cosmic Storm.mp3"),
            File("/storage/emulated/0/Music/Cymatics - Nigel Stanford.mp3"),
            File("/storage/emulated/0/Music/DANGER - 0.59 - MrSuicideSheep.mp3"),
            File("/storage/emulated/0/Music/Don't Play - JAEGER.mp3"),
            File("/storage/emulated/0/Music/Epic Suspense Trailer Music - AndrewG.mp3"),
            File("/storage/emulated/0/Music/Est-ce que tu m'aimes - Maître Gims.mp3"),
            File("/storage/emulated/0/Music/Fading Love - Day 7.mp3"),
            File("/storage/emulated/0/Music/Far Centaurus - Nigel Stanford.mp3"),
            File("/storage/emulated/0/Music/Final Frontier - Thomas Bergersen.mp3"),
            File("/storage/emulated/0/Music/Simon Curtis - Flesh.mp3"),
            File("/storage/emulated/0/Music/Fruit Punch - Rhythm Ninja.mp3"),
            File("/storage/emulated/0/Music/Glow (Vokon Remix) - Voyage.mp3"),
            File("/storage/emulated/0/Music/Guardian Music.mp3"),
            File("/storage/emulated/0/Music/Heaven and Hell - Jeremy Blake.mp3"),
            File("/storage/emulated/0/Music/Icarus - Ivan Torrent.mp3"),
            File("/storage/emulated/0/Music/In the Dark - Sophie and the Giants.mp3"),
            File("/storage/emulated/0/Music/Last Friday Night - Katy Perry.mp3"),
            File("/storage/emulated/0/Music/Last Time - Nerxa.mp3"),
            File("/storage/emulated/0/Music/Legends Never Die.mp3"),
            File("/storage/emulated/0/Music/melodysheep - Life Beyond 3 Trailer Music.mp3"),
            File("/storage/emulated/0/Music/Luh Me Like That - Leeyou & Danceey.mp3"),
            File("/storage/emulated/0/Music/Magic of Hong Kong.mp3"),
            File("/storage/emulated/0/Music/Match In The Rain - Alec Benjamin.mp3"),
            File("/storage/emulated/0/Music/Middle of the Night - Elley Duhé.mp3"),
            File("/storage/emulated/0/Music/Moonriser - Ivan Torrent.mp3"),
            File("/storage/emulated/0/Music/Nevada - Vicetone.mp3"),
            File("/storage/emulated/0/Music/Two Steps From Hell - None Shall Live.mp3"),
            File("/storage/emulated/0/Music/OUT OUT - Joel Corry x Jax Jones.mp3"),
            File("/storage/emulated/0/Music/Our Last Stand - Niklas Johansson.mp3"),
            File("/storage/emulated/0/Music/Over Endless Fields - David Celeste.mp3"),
            File("/storage/emulated/0/Music/Paradox (Intro) - Chris Potts.mp3"),
            File("/storage/emulated/0/Music/Miley Cyrus - Prisoner.mp3"),
            File("/storage/emulated/0/Music/Rain In Ibiza - Felix Jaehn.mp3"),
            File("/storage/emulated/0/Music/Rebirth - RVRSPLAY.mp3"),
            File("/storage/emulated/0/Music/She's Back - Arcane.mp3"),
            File("/storage/emulated/0/Music/Starship Super Heavy Testing This Month - spaceXcentric.mp3"),
            File("/storage/emulated/0/Music/Sun Mother - melodysheep.mp3"),
            File("/storage/emulated/0/Music/Supernatural - Kevin MacLeod.mp3"),
            File("/storage/emulated/0/Music/Take Control Space - cleanmindsounds.mp3"),
            File("/storage/emulated/0/Music/Taking Flight - Greg Dombrowski.mp3"),
            File("/storage/emulated/0/Music/Talon - Overwatch League Music.mp3"),
            File("/storage/emulated/0/Music/Terminant - Nihilore.mp3"),
            File("/storage/emulated/0/Music/The Business - Tiesto.mp3"),
            File("/storage/emulated/0/Music/The Vibe - MARSHVLL.mp3"),
            File("/storage/emulated/0/Music/Transitions - Cotton Niblett.mp3"),
            File("/storage/emulated/0/Music/Tomsize - Trap Life.mp3"),
            File("/storage/emulated/0/Music/United Through Fire - Dual Motion Music.mp3"),
            File("/storage/emulated/0/Music/Unleash My Halo.mp3"),
            File("/storage/emulated/0/Music/Violet Burn - Greg Dombrowski.mp3"),
            File("/storage/emulated/0/Music/Walk - Kwabs.mp3"),
            File("/storage/emulated/0/Music/Welcome to the Playground - Bea Miller.mp3"),
            File("/storage/emulated/0/Music/West Coast - OneRepublic.mp3"),
            File("/storage/emulated/0/Music/What Could Have Been - Sting.mp3"),
            File("/storage/emulated/0/Music/Where I'll Be Waiting - Rich Edwards.mp3"),
            File("/storage/emulated/0/Music/ABT X Topic X A7S - Your Love.mp3"),
            File("/storage/emulated/0/Music/closer - n u a g e s.mp3"),
            File("/storage/emulated/0/Music/Isolated - Ora.mp3")
        )
    }
}