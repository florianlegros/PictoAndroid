package com.example.pictopicto.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.pictopicto.databinding.ActivityMainBinding
import com.example.pictopicto.model.Categorie
import com.example.pictopicto.model.Phrase
import com.example.pictopicto.model.Pictogramme
import com.example.pictopicto.model.Question
import com.example.pictopicto.repository.CategorieRepository
import com.example.pictopicto.repository.PhraseRepository
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


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pictoAdapter: PictoAdapter
    private lateinit var categorieAdapter: CategorieAdapter
    private lateinit var questionsAdapter: ArrayAdapter<Question>
    private var pictogrammes = arrayListOf<Pictogramme>()
    private var phrase = arrayListOf<Pictogramme>()
    private var categories = arrayListOf<Categorie>()
    private var questions = arrayListOf<Question>()
    private lateinit var categorieRepository: CategorieRepository
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
            questionRepository.updateDatabase(this@MainActivity)
        }

        pictoAdapter = PictoAdapter(phrase, mTts)
        pictoAdapter.clicklistener = true


        val callback: ItemTouchHelper.Callback = ItemMoveCallback(pictoAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recycler)

        with(binding) {
            recycler.adapter = pictoAdapter
            recycler.setOnDragListener(MyDragListener())

            recycler2.adapter = PictoAdapter(pictogrammes, mTts)

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
                            pictogrammes.clear()
                            pictogrammes.addAll(categories[position].pictogrammes ?: ArrayList())
                            recycler2.adapter = PictoAdapter(pictogrammes, mTts)
                            pictoAdapter.notifyDataSetChanged()
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
                pictoAdapter.getItems()
                    .forEach { phrase += " ${it.pictoNom}" }
                speakOut(phrase)

            }
            motAmot.setOnClickListener {
                val phrase = pictoAdapter.getItems()
                if (phrase.isEmpty().not() && positionTts < phrase.size) {
                    val mot = phrase[positionTts]
                    speakOut(mot.pictoNom)
                    positionTts++
                } else {
                    positionTts = 0
                }

            }
            viderPhrase.setOnClickListener {
                pictoAdapter.clear()
            }
            sauverPhrase.setOnClickListener {
                if (pictoAdapter.getItems().size > 0 && selectedQuestion.questionId > 0) {
                    val temp = Phrase(
                        selectedQuestion,
                        phrase.toImmutableList()
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        phraseRepository.insertPhrase(temp)
                        phraseRepository.updateDatabase(this@MainActivity)
                    }

                    pictoAdapter.clear()
                }
            }
        }
    }

    override fun onInit(status: Int) {
    }

    private fun speakOut(message: String) {
        mTts.speak(message, TextToSpeech.QUEUE_FLUSH, null)
    }


}