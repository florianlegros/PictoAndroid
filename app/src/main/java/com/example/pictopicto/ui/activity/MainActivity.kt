package com.example.pictopicto.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.epmi_edu.terreplurielle.AudioAnalyzerLib
import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity
import com.example.pictopicto.databinding.ActivityMainBinding
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Phrase
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.model.Question
import com.example.pictopicto.repository.CategorieRepository
import com.example.pictopicto.repository.PhraseRepository
import com.example.pictopicto.repository.PictogrammeRepository
import com.example.pictopicto.repository.QuestionRepository
import com.example.pictopicto.ui.adapter.CategorieAdapter
import com.example.pictopicto.ui.adapter.PictoAdapter
import com.example.pictopicto.ui.listener.ItemMoveCallback
import com.example.pictopicto.ui.listener.MyDragListener
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
    private var pictogrammes = arrayListOf<Pictogramme>()
    private var phrase = arrayListOf<Pictogramme>()
    private var categories = arrayListOf<Categorie>()
    private var questions = arrayListOf<Question>()
    private lateinit var categorieRepository: CategorieRepository
    private lateinit var pictogrammeRepository: PictogrammeRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var phraseRepository: PhraseRepository
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
        pictogrammeRepository = PictogrammeRepository.getInstance(application)!!
        questionRepository = QuestionRepository.getInstance(application)!!
        phraseRepository = PhraseRepository.getInstance(application)!!
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
        pictogrammeRepository.pictogrammesByCategorie.observe(this) {
            if (it.isEmpty().not()) {
                pictogrammes.clear()
                pictogrammes.addAll(it)
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
            pictogrammeRepository.updateDatabase(this@MainActivity)
            questionRepository.updateDatabase(this@MainActivity)
        }

        phraseAdapter = PictoAdapter(phrase, mTts)
        pictoAdapter = PictoAdapter(pictogrammes, mTts)
        phraseAdapter.clicklistener = true


        val callback: ItemTouchHelper.Callback = ItemMoveCallback(phraseAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recycler)

        with(binding) {
            recycler.adapter = phraseAdapter
            recycler.setOnDragListener(MyDragListener())

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
                                pictogrammeRepository.getAllPictogrammeByCategorieId(categories[position].categorieId)
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
                var phrase = ""
                phraseAdapter.getItems()
                    .forEach { phrase += " ${it.pictoNom}" }
                speakOut(phrase)

            }
            motAmot.setOnClickListener {
                val phrase = phraseAdapter.getItems()
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
            }
            sauverPhrase.setOnClickListener {
                if (phraseAdapter.getItems().size > 0 && selectedQuestion.questionId > 0) {
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
            graph.setOnClickListener {
                var phrase = ""
                val file =
                    File(applicationContext.filesDir.absolutePath + "/audio/test.wav")
                val file2 = File(applicationContext.filesDir.absolutePath + "/audio/test2.wav")
                file.createNewFile()

                phraseAdapter.getItems()
                    .forEach { phrase += " ${it.pictoNom}" }

                mTts.synthesizeToFile(
                    phrase,
                    null,
                    file,
                    TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
                )

                mTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(p0: String?) {}
                    override fun onDone(p0: String?) {

                        runOnUiThread {

                            val pictograms: ArrayList<HashMap<String, Int>> = ArrayList()
                            val pictogrammes = phraseAdapter.getItems()

                            for (i in 0 until pictogrammes.size) {
                                val mot: Pictogramme = pictogrammes[i]
                                val pictogram: HashMap<String, Int> = HashMap()
                                pictogram[mot.pictoNom] = resources.getIdentifier(
                                    mot.pictoImgfile.replace(".png", "").lowercase(),
                                    "drawable",
                                    packageName
                                )
                                pictograms.add(pictogram)
                            }
                            AudioAnalyzerLib.startActivity(
                                phrase,
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

}