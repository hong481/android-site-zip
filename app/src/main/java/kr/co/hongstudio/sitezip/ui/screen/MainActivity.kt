package kr.co.hongstudio.sitezip.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.storage.FirebaseStorage
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.activity.BaseActivity
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.billing.BillingManager
import kr.co.hongstudio.sitezip.databinding.ActivityMainBinding
import kr.co.hongstudio.sitezip.util.DisplayUtil
import kr.co.hongstudio.sitezip.util.KeyboardUtil
import kr.co.hongstudio.sitezip.util.LogUtil
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val viewModel: MainViewModel by viewModel()
    private val billingManager: BillingManager by inject()
    private val displayUtil: DisplayUtil by inject()

    private lateinit var keyboardUtil: KeyboardUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        initBinding()
        initViewPager()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUseAdmob()
        
        // 전면광고 제어

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.adViewBanner.destroy()
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun initViewModel() {
        // 키보드 리스너 등록.
        keyboardUtil = KeyboardUtil(applicationContext, window)
        // 라이브데이터 옵저버.
        viewModel.isEnableContents.observe(this, Observer {
            Log.d(TAG, "isEnableContents : $it")
            if (it) {
                viewModel.getSiteZips()
            }
        })
        // 라이브데이터 옵저버.
        viewModel.isNetworkAvailable.observe(this, Observer {
            Log.d(TAG, "isNetworkAvailable : $it")
            if (it) {
                initBillingManager()
                if (viewModel.isUseAdmob.value == true) {
                    initAdViewBanner()
                }
            }
            viewModel.setShowNetworkErrorLayout(isShow = !it)
            if (viewModel.isUseAdmob.value == true) {
                viewModel.setShowBannerAds(isShow = it)
            }
        })
        viewModel.siteZips.observe(this, Observer {
            (binding.viewPager.adapter as? SiteZipsAdapter)?.setItems(it)
        })
        viewModel.searchVisibility.observe(this, Observer {
            if (it) {
                binding.etSearchText.requestFocus()
                keyboardUtil.visibleKeyboard(true, binding.etSearchText)
            } else {
                binding.etSearchText.setText("")
                keyboardUtil.visibleKeyboard(false, binding.etSearchText)
            }
        })
        viewModel.searchText.observe(this, Observer {
            LogUtil.d(TAG, "viewModel.searchText.observe.")
            viewModel.searchSites()
        })
        viewModel.setViewPagerUserInputEnabled.observe(this, Observer {
            binding.viewPager.isUserInputEnabled = it
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
        viewModel.isUseAdmob.observe(this, Observer {
            if (viewModel.isNetworkAvailable.value == true) {
                viewModel.setShowBannerAds(it)
                if(it) {
                    initAdViewBanner()
                    viewModel.checkShowInterstitialAd()
                }
            }
        })
        viewModel.billingRemoveAds.observe(this, EventObserver {
            billingManager.processToPurchase()
        })

        // 뷰모델 기본 옵저버.
        observeBaseViewModelEvent(viewModel)
        // 뷰 기본 세팅
        viewModel.setViewCheckNetwork()
    }

    /**
     * 뷰 페이저 초기화.
     */
    private fun initViewPager() = try {
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.offscreenPageLimit = 99
        binding.viewPager.adapter = SiteZipsAdapter(
            fragmentActivity = this
        )
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            val adapter: SiteZipsAdapter = (binding.viewPager.adapter as SiteZipsAdapter)
            val tabIconUrl: String = adapter.siteZips[position].tabIconUrl
            if (tabIconUrl.isNotEmpty()) {
                Glide.with(this)
                    .asBitmap()
                    .fitCenter()
                    .centerInside()
                    .load(
                        FirebaseStorage.getInstance()
                            .getReference(adapter.siteZips[position].tabIconUrl)
                    )
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            tab.icon = BitmapDrawable(resources, bitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
            val padding: Int = displayUtil.dpToPx(3f).toInt()
            (tab.view.getChildAt(0) as ImageView).setPadding(
                padding,
                padding,
                padding,
                padding
            )
            tab.text = adapter.siteZips[position].typeName
        }.attach()
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }

    /**
     * 애드몹 배너 초기화.
     */
    private fun initAdViewBanner() {
        binding.adViewBanner.loadAd(AdRequest.Builder().build())
    }

    /**
     * 인앱 결제 초기화.
     */
    private fun initBillingManager() {
        billingManager.connectGooglePlay()
    }

    /**
     * 뒤로가기 버튼 클릭시 홈으로 이동.
     */
    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}