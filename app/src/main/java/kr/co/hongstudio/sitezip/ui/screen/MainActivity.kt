package kr.co.hongstudio.sitezip.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.hongstudio.sitezip.App
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.base.activity.BaseActivity
import kr.co.hongstudio.sitezip.base.livedata.EventObserver
import kr.co.hongstudio.sitezip.billing.BillingManager
import kr.co.hongstudio.sitezip.data.local.entity.SiteZip
import kr.co.hongstudio.sitezip.data.local.preference.AppPreference
import kr.co.hongstudio.sitezip.databinding.ActivityMainBinding
import kr.co.hongstudio.sitezip.glide.GlideApp
import kr.co.hongstudio.sitezip.ui.appirater.AppiraterDialog
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipFragment
import kr.co.hongstudio.sitezip.ui.screen.place.PlaceZipViewModel
import kr.co.hongstudio.sitezip.ui.screen.setting.SettingFragment
import kr.co.hongstudio.sitezip.ui.screen.site.SiteZipFragmentAdapter
import kr.co.hongstudio.sitezip.util.DisplayUtil
import kr.co.hongstudio.sitezip.util.KeyboardUtil
import kr.co.hongstudio.sitezip.util.extension.lifecycleFragmentManager
import kr.co.hongstudio.sitezip.util.extension.observeBaseViewModelEvent
import kr.co.hongstudio.sitezip.util.extension.timer
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

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
    private val placeZipViewModel: PlaceZipViewModel by viewModel()

    private val billingManager: BillingManager by inject()
    private val displayUtil: DisplayUtil by inject()
    private val appPref: AppPreference by inject()

    private val placeZipFragment: PlaceZipFragment by inject()
    private val settingFragment: SettingFragment by inject()

    private val appiraterDialogDisposable = CompositeDisposable()

    lateinit var keyboardUtil: KeyboardUtil

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
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.adViewBanner.destroy()
    }

    private fun initBinding() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
        binding.bottomNav.menu.findItem(R.id.navigation_place).isEnabled = false
    }

    private fun initViewModel() {
        // 키보드 리스너 등록.
        keyboardUtil = KeyboardUtil(applicationContext, window)

        // 라이브데이터 옵저버.
        viewModel.isEnableContents.observe(this, Observer {
            Log.d(TAG, "isEnableContents : $it")
            if (it) {
                viewModel.registerZipSizeListener()
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
        viewModel.zipSize.observe(this, Observer {
            Log.d(TAG, "siteZipSize: $it")
            (binding.viewPager.adapter as? SiteZipFragmentAdapter)?.setSize(it)
            if (viewModel.siteZipList.size <= 0) {
                viewModel.registerZipsListener()
            } else {
                App.restart(this, createIntent(applicationContext, true))
            }
        })
        viewModel.siteZips.observe(this, Observer {
            val items: MutableList<SiteZip> = it.toMutableList()
            (binding.viewPager.adapter as? SiteZipFragmentAdapter)?.setItems(
                items = items.apply {
                    sortBy { item -> item.index }
                }
            )
        })
        viewModel.placeZip.observe(this, Observer {
            if (it != null && !binding.bottomNav.menu.findItem(R.id.navigation_place).isEnabled) {
                binding.bottomNav.menu.findItem(R.id.navigation_place).isEnabled = true
                placeZipViewModel.onBind(it)
            }
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
            Log.d(TAG, "viewModel.searchText.observe.")
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
                if (it) {
                    initAdViewBanner()
                    viewModel.checkShowInterstitialAd()
                }
            }
        })
        // 화면 제어
        viewModel.replaceSiteScreen.observe(this, EventObserver {
            viewModel.setVisibleSiteScreen(true)
            viewModel.setVisiblePlaceScreen(false)
            viewModel.setVisibleSettingScreen(false)
        })
        viewModel.replacePlaceScreen.observe(this, EventObserver {
            viewModel.setVisibleSiteScreen(false)
            viewModel.setVisiblePlaceScreen(true)
            viewModel.setVisibleSettingScreen(false)
            if (supportFragmentManager.findFragmentByTag(PlaceZipFragment.TAG) != null) {
                supportFragmentManager.beginTransaction().let {
                    it.show(placeZipFragment)
                    it.hide(settingFragment)
                    it.commit()
                }
            } else {
                placeZipFragment.add(
                    fragmentManager = supportFragmentManager,
                    container = binding.frameLayout,
                    tag = PlaceZipFragment.TAG
                )
            }
        })
        viewModel.replaceSettingScreen.observe(this, EventObserver {
            viewModel.setVisibleSiteScreen(false)
            viewModel.setVisiblePlaceScreen(false)
            viewModel.setVisibleSettingScreen(true)
            if (supportFragmentManager.findFragmentByTag(SettingFragment.TAG) != null) {
                supportFragmentManager.beginTransaction().let {
                    it.show(settingFragment)
                    it.hide(placeZipFragment)
                    it.commit()
                }
            } else {
                settingFragment.add(
                    fragmentManager = supportFragmentManager,
                    container = binding.frameLayout,
                    tag = SettingFragment.TAG
                )
            }
        })
        viewModel.billingRemoveAds.observe(this, EventObserver {
            billingManager.processToPurchase(BillingManager.REMOVE_ADS, this)
        })
        viewModel.billingSponsor.observe(this, EventObserver {
            billingManager.processToPurchase(BillingManager.SUPPORT, this)
        })
        viewModel.disposeAppiraterDialogEvent.observe(this, EventObserver {
            appiraterDialogDisposable.dispose()
        })
        // 뷰모델 기본 옵저버.
        observeBaseViewModelEvent(viewModel)
        // 리뷰 요청 팝업 창.
        initAppiraterDialog()
        // 뷰 기본 세팅.
        viewModel.setViewCheckNetwork()
    }

    /**
     * 뷰 페이저 초기화.
     */
    private fun initViewPager() = try {
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter =
            SiteZipFragmentAdapter(
                fragmentActivity = this
            )

        val adapterSiteZip: SiteZipFragmentAdapter =
            (binding.viewPager.adapter as SiteZipFragmentAdapter)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            Log.d(
                TAG,
                "adapter.siteZips.size : ${adapterSiteZip.adapterItems.size} " +
                        "/ " +
                        "adapter.siteZipsSize: ${adapterSiteZip.adapterItemsSize}"
            )
            // 리턴.
            if (adapterSiteZip.adapterItems.size < adapterSiteZip.adapterItemsSize) {
                Log.d(TAG, "TabLayoutMediator. return.")
                return@TabLayoutMediator
            }
            val siteZip: SiteZip = adapterSiteZip.adapterItems[position]
            val tabIconUrl: String = siteZip.tabIconUrl
            tab.text = siteZip.tabName
            val padding: Int = displayUtil.dpToPx(3f).toInt()
            (tab.view.getChildAt(0) as ImageView).setPadding(padding, padding, padding, padding)
            Log.d(TAG, "position : $position / tabIconUrl : $tabIconUrl")
            if (tabIconUrl.isNotEmpty()) {
                GlideApp.with(applicationContext)
                    .asDrawable()
                    .load(FirebaseStorage.getInstance().getReference(tabIconUrl))
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            tab.icon = null
                            tab.icon = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }.attach()
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }

    /**
     * 앱 리뷰 다이어로그 초기화.
     */
    private fun initAppiraterDialog() {
        val appiraterDialog: AppiraterDialog =
            AppiraterDialog.newInstance(object : AppiraterDialog.AppiraterDialogCallback {
                override fun onDismiss() {
                    viewModel.disposeAppiraterDialog()
                }
            })
        Flowable.interval(10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onBackpressureBuffer()
            .subscribeBy(
                onNext = {
                    if (viewModel.checkVisibleProgress.value == false
                        && appPref.visibleAppiraterDialog
                    ) {
                        timer(5) {
                            appiraterDialog.show(lifecycleFragmentManager, AppiraterDialog.TAG)
                        }
                    } else if (!appPref.visibleAppiraterDialog) {
                        viewModel.disposeAppiraterDialog()
                    }
                },
                onError = {
                    Log.d(TAG, it.toString())
                }
            ).let {
                appiraterDialogDisposable += it
            }
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
     * 사이트 네비게이션 선택.
     */
    private fun selectSiteNavigation() {
        viewModel.setSearchText("")
        viewModel.setSearchVisibility(false)
        viewModel.setSearchButtonVisible(true)
        viewModel.setFavoriteButtonVisible(true)
        viewModel.replaceSiteScreen()
    }

    /**
     * 장소 네비게이션 선택.
     */
    private fun selectPlaceNavigation() {
        viewModel.setSearchText("")
        viewModel.setSearchVisibility(false)
        viewModel.setSearchButtonVisible(false)
        viewModel.setFavoriteButtonVisible(false)
        viewModel.replacePlaceScreen()
    }

    /**
     * 장소 설정 선택.
     */
    private fun selectSettingNavigation() {
        viewModel.setSearchText("")
        viewModel.setSearchVisibility(false)
        viewModel.setSearchButtonVisible(false)
        viewModel.setFavoriteButtonVisible(false)
        viewModel.replaceSettingScreen()
    }

    /**
     * 뒤로가기 버튼 클릭시 홈으로 이동.
     */
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_site -> {
                selectSiteNavigation()
            }
            R.id.navigation_place -> {
                selectPlaceNavigation()
            }
            R.id.navigation_setting -> {
                selectSettingNavigation()
            }
        }
        return true
    }
}