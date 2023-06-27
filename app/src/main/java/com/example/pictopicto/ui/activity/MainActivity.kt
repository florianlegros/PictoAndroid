package com.example.pictopicto.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.DragEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.epmi_edu.terreplurielle.AudioAnalyzerLib
import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity
import com.example.pictopicto.databinding.ActivityMainBinding
import com.example.pictopicto.model.*
import com.example.pictopicto.repository.*
import com.example.pictopicto.ui.adapter.CategorieAdapter
import com.example.pictopicto.ui.adapter.PictoAdapter
import com.example.pictopicto.ui.listener.ItemMoveCallback
import com.example.pictopicto.ui.listener.RecyclerItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import java.io.File

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var phraseAdapter: PictoAdapter
    private lateinit var pictoAdapter: PictoAdapter
    private lateinit var categorieAdapter: CategorieAdapter
    private lateinit var questionsAdapter: ArrayAdapter<Question>

    private var mots = arrayListOf<Mot>()
    private var phrase = arrayListOf<Mot>()
    private var phraseCorect = ""
    private var categories = arrayListOf<Categorie>()
    private var questions = arrayListOf<Question>()
    private var temps = "present"
    private var negatif = false

    private lateinit var categorieRepository: CategorieRepository
    private lateinit var motRepository: MotRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var phraseRepository: PhraseRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var irregulierRepository: IrregulierRepository
    private lateinit var conjugaisonRepository: ConjugaisonRepository

    private lateinit var mTts: TextToSpeech
    private var positionTts: Int = 0
    private var selectedQuestion: Question = Question(-1, "Choisir une question", ArrayList())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mTts = TextToSpeech(this, this)
        mTts.setSpeechRate(0.8f)

        categorieRepository = CategorieRepository.getInstance(application)!!
        motRepository = MotRepository.getInstance(application)!!
        questionRepository = QuestionRepository.getInstance(application)!!
        phraseRepository = PhraseRepository.getInstance(application)!!
        tagRepository = TagRepository.getInstance(application)!!
        irregulierRepository = IrregulierRepository.getInstance(application)!!
        conjugaisonRepository = ConjugaisonRepository.getInstance(application)!!


        categorieAdapter = CategorieAdapter(categories)
        questionsAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, questions)
        questionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorieRepository.categories.observe(this) {
            if (it.isEmpty().not()) {
                categories.clear()
                categories.addAll(it)
                categorieAdapter.notifyDataSetChanged()
            }
        }
        motRepository.pictogrammesByCategorie.observe(this) {
            if (it.isEmpty().not()) {
                mots.clear()
                mots.addAll(it)
                pictoAdapter.notifyDataSetChanged()
            }
        }

        questionRepository.questions.observe(this) {
            if (it.isEmpty().not()) {
                questions.clear()
                questions.add(Question(-1, "Choisir une question", ArrayList()))
                questions.addAll(it)
                questionsAdapter.notifyDataSetChanged()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            categorieRepository.updateDatabase(this@MainActivity)
            motRepository.updateDatabase(this@MainActivity)
            questionRepository.updateDatabase(this@MainActivity)
        }

        phraseAdapter = PictoAdapter(phrase, mTts)
        pictoAdapter = PictoAdapter(mots, mTts)
        phraseAdapter.clicklistener = true


        val callback: ItemTouchHelper.Callback = ItemMoveCallback(phraseAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recycler)

        with(binding) {
            recycler.adapter = phraseAdapter

            recycler.setOnDragListener { v: View, e: DragEvent ->
                when (e.action) {
                    //event quand le drag est stopper
                    DragEvent.ACTION_DRAG_EXITED -> {
                        (e.localState as View).visibility = View.VISIBLE
                    }
                    //event quand l'objet est drop
                    DragEvent.ACTION_DROP -> {
                        //recuperes l'endroit du drop (ici c'est un recycler)
                        val container = v as RecyclerView
                        //recuperes les données de l'objet (ici c'est juste un string "nom de l'image")
                        val item = e.clipData.getItemAt(0).intent.getSerializableExtra("item")
                        //verifies si on a bien recup le nom de l'image
                        if (item != null) {
                            //recup l'adapter du recyler et on ajoute l'image
                            val adapter: PictoAdapter =
                                container.adapter as PictoAdapter
                            adapter.addItem(item as Mot)

                            //verif si ya deja une image en dessous
                            val intercept =
                                container.findChildViewUnder(e.x, e.y)
                            //si oui on echange les places
                            if (intercept != null && container.getChildAdapterPosition(intercept) != -1) {
                                adapter.onRowMoved(
                                    (adapter.itemCount - 1),
                                    container.getChildAdapterPosition(intercept)
                                )
                            }
                        }
                        corrector()
                    }
                    //event quand le drag est arreter
                    DragEvent.ACTION_DRAG_ENDED -> {
                        (e.localState as View).visibility = View.VISIBLE
                    }
                    else -> {}
                }
                return@setOnDragListener true
            }

            recycler2.adapter = pictoAdapter

            recycler3.adapter = categorieAdapter
            recycler3.addOnItemTouchListener(
                RecyclerItemClickListener(
                    this@MainActivity,
                    recycler3,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {

                            speakOut(
                                categories[position].categorieNom
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                motRepository.getAllPictogrammeByCategorieId(categories[position].categorieId)
                            }
                        }

                        override fun onItemLongClick(view: View?, position: Int) {
                        }
                    })
            )
            spinner.adapter = questionsAdapter
            spinner.setSelection(0)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    if (adapterView != null && i > 0) {
                        selectedQuestion = adapterView.adapter.getItem(i) as Question
                        speakOut(selectedQuestion.contenu)
                        categories.clear()
                        categories.addAll(selectedQuestion.categories as ArrayList<Categorie>)
                    } else {
                        categories.clear()
                        categorieRepository.categories.value?.let { categories.addAll(it) }
                    }
                    categorieAdapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    spinner.setSelection(0)
                }

            }

            lirePhrase.setOnClickListener {
                speakOut(phraseCorect)
            }
            motAmot.setOnClickListener {
                if (phrase.isEmpty().not() && positionTts < phrase.size) {
                    val mot = phrase[positionTts]
                    speakOut(mot.pictoNom)
                    positionTts++
                } else {
                    positionTts = 0
                }

            }
            viderPhrase.setOnClickListener {
                phraseAdapter.clear()
                phraseCorect = ""
                phraseText.text = ""
            }
            sauverPhrase.setOnClickListener {
                if (phraseAdapter.getItems().size > 0) {
                    val temp = Phrase(
                        selectedQuestion,
                        phrase.toImmutableList()
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        phraseRepository.insertPhrase(temp)
                        phraseRepository.updateDatabase(this@MainActivity)
                    }

                    phraseAdapter.clear()
                }
            }

            tempsFutur.setOnClickListener {
                temps = "futur"
                corrector()
            }
            tempsPasse.setOnClickListener {
                temps = "futur"
                corrector()
            }
            phraseNormal.setOnClickListener {
                temps = "present"
                negatif = false
                corrector()
            }
            delLast.setOnClickListener {
                phrase.removeLast()
                corrector()
            }
            graph.setOnClickListener {
                var out = ""
                val file =
                    File(applicationContext.filesDir.absolutePath + "/audio/test.wav")

                file.createNewFile()

                phraseAdapter.getItems()
                    .forEach { out += " ${it.pictoNom}" }

                mTts.synthesizeToFile(
                    out,
                    null,
                    file,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                )

                mTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(p0: String?) {}
                    override fun onDone(p0: String?) {

                        runOnUiThread {

                            val pictograms: ArrayList<HashMap<String, Int>> = ArrayList()

                            for (i in 0 until phrase.size) {
                                val mot: Mot = phrase[i]
                                val pictogram: HashMap<String, Int> = HashMap()
                                pictogram[mot.pictoNom] = resources.getIdentifier(
                                    mot.pictoImgfile.replace(".png", "").lowercase(),
                                    "drawable",
                                    packageName
                                )
                                pictograms.add(pictogram)
                            }
                            AudioAnalyzerLib.startActivity(
                                out,
                                pictograms,
                                applicationContext,
                                resources.displayMetrics.density,
                                this@MainActivity,
                                AudioAnalyzerActivity::class.java
                            )
                        }


                    }

                    override fun onError(p0: String?) {}
                })

            }
        }
    }

    override fun onInit(status: Int) {
    }

    private fun speakOut(message: String) {
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun corrector() {
        var pronom = ""
        phraseCorect = ""
        phrase
            .forEach {
                var nom = it.pictoNom + " "
                with(it.tags) {
                    if (contains(Tag("irregulier"))) {
                        when {
                            contains(Tag("verbe"))
                            -> {

                                when (temps) {
                                    "present" -> phraseCorect += when (pronom) {
                                        "je" -> it.irregulier!!.conjugaison[0].premiere_pers_sing
                                        "tu" -> it.irregulier!!.conjugaison[0].deuxieme_pers_sing
                                        "il" -> it.irregulier!!.conjugaison[0].troisieme_pers_sing
                                        "elle" -> it.irregulier!!.conjugaison[0].troisieme_pers_sing
                                        "on" -> it.irregulier!!.conjugaison[0].troisieme_pers_sing
                                        "nous" -> it.irregulier!!.conjugaison[0].premiere_pers_pluriel
                                        "vous" -> it.irregulier!!.conjugaison[0].deuxieme_pers_pluriel
                                        "ils" -> it.irregulier!!.conjugaison[0].troisieme_pers_pluriel
                                        "elles" -> it.irregulier!!.conjugaison[0].troisieme_pers_pluriel
                                        "ont" -> it.irregulier!!.conjugaison[0].troisieme_pers_pluriel
                                        else -> nom
                                    }
                                    "futur" -> phraseCorect += when (pronom) {
                                        "je" -> it.irregulier!!.conjugaison[1].premiere_pers_sing
                                        "tu" -> it.irregulier!!.conjugaison[1].deuxieme_pers_sing
                                        "il" -> it.irregulier!!.conjugaison[1].troisieme_pers_sing
                                        "elle" -> it.irregulier!!.conjugaison[1].troisieme_pers_sing
                                        "on" -> it.irregulier!!.conjugaison[1].troisieme_pers_sing
                                        "nous" -> it.irregulier!!.conjugaison[1].premiere_pers_pluriel
                                        "vous" -> it.irregulier!!.conjugaison[1].deuxieme_pers_pluriel
                                        "ils" -> it.irregulier!!.conjugaison[1].troisieme_pers_pluriel
                                        "elles" -> it.irregulier!!.conjugaison[1].troisieme_pers_pluriel
                                        "ont" -> it.irregulier!!.conjugaison[1].troisieme_pers_pluriel
                                        else -> nom
                                    }
                                    "passe" -> phraseCorect += when (pronom) {
                                        "je" -> it.irregulier!!.conjugaison[2].premiere_pers_sing
                                        "tu" -> it.irregulier!!.conjugaison[2].deuxieme_pers_sing
                                        "il" -> it.irregulier!!.conjugaison[2].troisieme_pers_sing
                                        "elle" -> it.irregulier!!.conjugaison[2].troisieme_pers_sing
                                        "on" -> it.irregulier!!.conjugaison[2].troisieme_pers_sing
                                        "nous" -> it.irregulier!!.conjugaison[2].premiere_pers_pluriel
                                        "vous" -> it.irregulier!!.conjugaison[2].deuxieme_pers_pluriel
                                        "ils" -> it.irregulier!!.conjugaison[2].troisieme_pers_pluriel
                                        "elles" -> it.irregulier!!.conjugaison[2].troisieme_pers_pluriel
                                        "ont" -> it.irregulier!!.conjugaison[2].troisieme_pers_pluriel
                                        else -> nom
                                    }
                                }
                                if (phraseCorect.contains(" se ")){
                                    phraseCorect = phraseCorect.replace(" se ", when (pronom) {
                                        "je" -> " me "
                                        "tu" -> " te "
                                        "nous" -> " nous "
                                        "vous" -> " vous "
                                        else -> " se "
                                    })
                                }
                                else if (phraseCorect.contains(" s'")){
                                    phraseCorect = phraseCorect.replace(" s'", when (pronom) {
                                        "je" -> " m'"
                                        "tu" -> " t'"
                                        "nous" -> " nous "
                                        "vous" -> " vous "
                                        else -> " s'"
                                    })
                                }
                                pronom = ""
                            }
                            else -> phraseCorect += nom
                        }
                        phraseCorect += " "
                    } else {
                        when {
                            contains(Tag("pronom_ou_determinant")) -> {
                                pronom = it.pictoNom
                                phraseCorect += nom
                            }
                            contains(Tag("verbe")) -> {

                                if (nom.contains("se")) {
                                    nom = nom.replace(
                                        "se", when (pronom) {
                                            "je" -> "me"
                                            "tu" -> "te"
                                            "nous" -> "nous"
                                            "vous" -> "vous"
                                            else -> "se"
                                        }
                                    )
                                } else if (nom.contains("s'")) {
                                    nom = nom.replace(
                                        "s'", when (pronom) {
                                            "je" -> "m'"
                                            "tu" -> "t'"
                                            "nous" -> "nous "
                                            "vous" -> "vous "
                                            else -> "s'"
                                        }
                                    )
                                }

                                if (temps == "present") {
                                    if (contains(Tag("premier_groupe"))) {
                                        phraseCorect += when (pronom) {
                                            "je" -> nom.replaceFirst("er ", "e ")
                                            "tu" -> nom.replaceFirst("er ", "es ")
                                            "il" -> nom.replaceFirst("er ", "e ")
                                            "elle" -> nom.replaceFirst("er ", "e ")
                                            "on" -> nom.replaceFirst("er ", "e ")
                                            "nous" -> nom.replaceFirst("er ", "ons ")
                                            "vous" -> nom.replaceFirst("er ", "ez ")
                                            "ils" -> nom.replaceFirst("er ", "ent ")
                                            "elles" -> nom.replaceFirst("er ", "ent ")
                                            "ont" -> nom.replaceFirst("er ", "ent ")
                                            else -> nom
                                        }
                                    } else if (contains(Tag("deuxieme_groupe"))) {
                                        phraseCorect += when (pronom) {
                                            "je" -> nom.replaceFirst("ir ", "is ")
                                            "tu" -> nom.replaceFirst("ir ", "is ")
                                            "il" -> nom.replaceFirst("ir ", "it ")
                                            "elle" -> nom.replaceFirst("ir ", "it ")
                                            "on" -> nom.replaceFirst("ir ", "it ")
                                            "nous" -> nom.replaceFirst("ir ", "issons ")
                                            "vous" -> nom.replaceFirst("ir ", "issez ")
                                            "ils" -> nom.replaceFirst("ir ", "issent ")
                                            "elles" -> nom.replaceFirst("ir ", "issent ")
                                            "ont" -> nom.replaceFirst("ir ", "issent ")
                                            else -> nom
                                        }
                                    }

                                } else if (temps == "futur") {
                                    if (contains(Tag("premier_groupe"))) {
                                        phraseCorect += when (pronom) {
                                            "je" -> nom.replaceFirst("er ", "erai ")
                                            "tu" -> nom.replaceFirst("er ", "eras ")
                                            "il" -> nom.replaceFirst("er ", "era ")
                                            "elle" -> nom.replaceFirst("er ", "era ")
                                            "on" -> nom.replaceFirst("er ", "era ")
                                            "nous" -> nom.replaceFirst("er ", "erons ")
                                            "vous" -> nom.replaceFirst("er ", "erez ")
                                            "ils" -> nom.replaceFirst("er ", "eront ")
                                            "elles" -> nom.replaceFirst("er ", "eront ")
                                            "ont" -> nom.replaceFirst("er ", "eront ")
                                            else -> nom
                                        }
                                    } else if (contains(Tag("deuxieme_groupe"))) {
                                        phraseCorect += when (pronom) {
                                            "je" -> nom.replaceFirst("ir ", "irai ")
                                            "tu" -> nom.replaceFirst("ir ", "iras ")
                                            "il" -> nom.replaceFirst("ir ", "ira ")
                                            "elle" -> nom.replaceFirst("ir ", "ira ")
                                            "on" -> nom.replaceFirst("ir ", "ira ")
                                            "nous" -> nom.replaceFirst("ir ", "irons ")
                                            "vous" -> nom.replaceFirst("ir ", "irez ")
                                            "ils" -> nom.replaceFirst("ir ", "iront ")
                                            "elles" -> nom.replaceFirst("ir ", "iront ")
                                            "ont" -> nom.replaceFirst("ir ", "iront ")
                                            else -> nom
                                        }
                                    }
                                }

                                pronom = ""
                            }
                            else -> phraseCorect += nom
                        }
                    }
                }
                phraseCorect =
                    phraseCorect.replaceFirst(
                        regex = Regex("[aeio] ([aeiou])"),
                        replacement = "'$1"
                    )


                binding.phraseText.text = phraseCorect
            }
    }
}
