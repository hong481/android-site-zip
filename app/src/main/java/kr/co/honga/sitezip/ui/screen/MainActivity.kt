package kr.co.honga.sitezip.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.honga.sitezip.R
import kr.co.honga.sitezip.base.activity.BaseActivity
import kr.co.honga.sitezip.base.livedata.EventObserver
import kr.co.honga.sitezip.data.BuildProperty
import kr.co.honga.sitezip.databinding.ActivityMainBinding
import kr.co.honga.sitezip.util.KeyboardUtil
import kr.co.honga.sitezip.util.LogUtil
import kr.co.honga.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject


class MainActivity : BaseActivity() {

    companion object {
        const val TAG: String = "MainActivity"

        fun createIntent(context: Context, isNewTask: Boolean = false): Intent =
            Intent(context, MainActivity::class.java).apply {
                when (isNewTask) {
                    true -> {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    false -> {
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                }
            }

        fun intent(
            context: Context
        ) = createIntent(
            context
        ).also { intent ->
            context.startActivity(intent)
        }
    }

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val viewModel: MainViewModel by viewModel()
    private val buildProperty: BuildProperty by inject()

    private lateinit var keyboardUtil: KeyboardUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViewPager()
        initViewModel()

        if(buildProperty.useGoogleAdmob) {
            initAdViewBanner()
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun initViewModel() {
        // 키보드 리스너 등록.
        keyboardUtil = KeyboardUtil(applicationContext, window)
        // 사이트 정보 가져오기.
        viewModel.getSiteTypes()

        viewModel.siteZips.observe(this, Observer {
            (binding.viewPager.adapter as? SiteZipsAdapter)?.setItems(it)
        })

        // 라이브데이터 옵저버.
        viewModel.searchVisibility.observe(this, Observer {
            if (it) {
                binding.etSearchText.requestFocus()
                keyboardUtil.visibleKeyboard(true, binding.etSearchText)
            } else {
                keyboardUtil.visibleKeyboard(false, binding.etSearchText)
            }
        })
        viewModel.searchText.observe(this, Observer {
            LogUtil.d(TAG, "viewModel.searchText.observe.")
            viewModel.searchSites()
        })
        viewModel.playVoiceSearch.observe(this, EventObserver {
            OuterActivities.intentVoiceSearch(this) {
                if (it.data != null) {
                    val textArray: ArrayList<String>? =
                        it.data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    textArray.let {
                        viewModel.setSearchText(textArray?.get(0) ?: "")
                    }
                }
            }
        })
        // 뷰모델 기본 옵저버.
        observeBaseViewModelEvent(viewModel)
    }

    /**
     * 뷰 페이저 초기화.
     */
    private fun initViewPager() {
        binding.viewPager.adapter = SiteZipsAdapter(
            fragmentActivity = this
        )
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            tab.text = (binding.viewPager.adapter as SiteZipsAdapter).siteZips[position].typeName
        }.attach()
    }

    /**
     * 애드몹 배너 초기화.
     */
    private fun initAdViewBanner() {
        binding.adViewBanner.loadAd(AdRequest.Builder().build())
    }
}