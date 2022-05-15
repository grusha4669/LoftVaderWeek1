package ru.pashaginas.myapplication.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.internal.toImmutableList
import ru.pashaginas.myapplication.LoftApp
import ru.pashaginas.myapplication.MoneyItemDataClass
import ru.pashaginas.myapplication.R
import ru.pashaginas.myapplication.adapters.MoneyItemsAdapter
import ru.pashaginas.myapplication.adapters.ViewPagerAdapter
import ru.pashaginas.myapplication.remote.MoneyApi
import ru.pashaginas.myapplication.remote.MoneyRemoteItem
import ru.pashaginas.myapplication.remote.MoneyResponse
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var itemsAdapter: MoneyItemsAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var compositeDisposable: CompositeDisposable

    companion object {
        const val RESULT_CODE = 500
    }


    private val fablistener = View.OnClickListener { view ->
        when (view.id) {
            R.id.fab -> {
                val intent = Intent(this, AddItemActivity::class.java)
                startActivityForResult(intent, RESULT_CODE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener(fablistener)

        //todo rw to fragment class
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView_main)
        recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        itemsAdapter = MoneyItemsAdapter()
        recyclerView.adapter = itemsAdapter

        recyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = viewPagerAdapter

        tabLayout = findViewById(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.fragment_a)
                1 -> getString(R.string.fragment_b)
                2 -> getString(R.string.fragment_c)
                else -> {
                    throw Resources.NotFoundException("Fragment not found")
                }
            }
        }.attach()
    }

    //??
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_CODE == RESULT_CODE && data != null) {
            itemsAdapter.addItem(
                MoneyItemDataClass(
                    data.getStringExtra(AddItemActivity.KEY_AMOUNT)?.toInt() ?: 0,
                    data.getStringExtra(AddItemActivity.KEY_PURPOSE) ?: ""
                )
            )
        }
    }

    fun fetchData() {
        val disposable: Disposable = (getApplication() as LoftApp).moneyApi?.getMoneyItems("income")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ moneyResponse ->
                if (moneyResponse.status.equals("success")) {
                    val moneyItems: MutableList<MoneyItemDataClass> = ArrayList<MoneyItemDataClass>()
                    for (moneyRemoteItem in moneyResponse.moneyItemsList!!) {
                        moneyItems.add(MoneyItemDataClass.getInstance(moneyRemoteItem))
                    }
                    itemsAdapter.addItem(MoneyItemDataClass("".toInt(),""))
                } else {
                    Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.add),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }) { throwable ->
                Toast.makeText(
                    getApplicationContext(),
                    throwable.getLocalizedMessage(),
                    Toast.LENGTH_LONG
                ).show()
            }!!
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
