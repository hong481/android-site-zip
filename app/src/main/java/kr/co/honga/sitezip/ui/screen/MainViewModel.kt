package kr.co.honga.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.data.local.entity.SiteType
import kr.co.honga.sitezip.firebase.FireBaseDatabaseUtil
import kr.co.honga.sitezip.repositories.repository.SiteRepository

class MainViewModel(

    private val fireBaseDatabaseUtil: FireBaseDatabaseUtil,
    private val siteRepository: SiteRepository

) : BaseViewModel() {

    companion object {
        const val TAG: String = "MainViewModel"
    }

    private val _siteTypes: MutableLiveData<MutableList<SiteType>> = MutableLiveData()
    val siteTypes: LiveData<MutableList<SiteType>> = _siteTypes

    /**
     * 검색 이벤트.
     */
    private val _searchVisibility: MutableLiveData<Boolean> = MutableLiveData()
    val searchVisibility: LiveData<Boolean> = _searchVisibility

    /**
     * 검색어.
     */
    private val _searchText: MutableLiveData<String> = MutableLiveData()
    val searchText: MutableLiveData<String> = _searchText

    /**
     * 검색 텍스트 변경 여부.
     */
    private val _isSearchTextChanged: MutableLiveData<Boolean> = MutableLiveData()
    val isSearchTextChanged: LiveData<Boolean> = _isSearchTextChanged

    /**
     * 즐겨찾기만 표시 여부.
     */
    private val _isFavoriteMode: MutableLiveData<Boolean> = MutableLiveData()
    val isFavoriteMode: LiveData<Boolean> = _isFavoriteMode

    fun getSites() {
        showProgress(true)
        fireBaseDatabaseUtil.database.getReference(FireBaseDatabaseUtil.SITES_PATH)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d(TAG, dataSnapshot.childrenCount.toString())
                        siteRepository.getAllSites()
                            .firstOrError()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onSuccess = { allSites ->
                                    val siteTypes: ArrayList<SiteType> = arrayListOf()
                                    for (typeSnapshot: DataSnapshot in dataSnapshot.children) {
                                        val siteType: SiteType? = typeSnapshot.getValue(SiteType::class.java).apply {
                                                this?.typeName = typeSnapshot.key ?: ""
                                                typeSnapshot.children.forEach { dataSnapshot ->
                                                    val site: Site? = dataSnapshot.getValue(Site::class.java).apply {
                                                        this?.sitePrimaryKey = "${typeSnapshot.key}_${dataSnapshot.key}"
                                                    }
                                                    for (chooseFavoriteSite: Site in allSites) {
                                                        if (chooseFavoriteSite.sitePrimaryKey == site?.sitePrimaryKey) {
                                                            site.isFavorite = true
                                                            break
                                                        }
                                                    }
                                                    Log.d(
                                                        TAG,
                                                        "key : ${dataSnapshot.key} / site.isFavorite: ${site?.isFavorite}"
                                                    )
                                                    this?.siteList?.add(site ?: return@apply)
                                                }
                                            }
                                        if (siteType != null) {
                                            siteTypes.add(siteType)
                                        }
                                    }
                                    _siteTypes.value = siteTypes
                                    dismissProgress(true)
                                },
                                onError = { throwable ->
                                    throwable.printStackTrace()
                                }
                            )
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(
                            FireBaseDatabaseUtil.TAG,
                            "loadPost:onCancelled",
                            databaseError.toException()
                        )
                    }
                })
    }


    /**
     * 내역 검색.
     */
    fun searchSites() {
        _isSearchTextChanged.value = !searchText.value.isNullOrEmpty()
    }

    /**
     * 검색 텍스트 클리어.
     */
    fun clearSearchText() {
        _searchText.value = ""
    }

    /**
     * 검색 레이아웃 활성화 여부.
     */
    fun setSearchVisibility(isEnable: Boolean) {
        _searchVisibility.value = isEnable
    }

    /**
     * 즐겨찾기만 표시 여부.
     */
    fun setFavoriteMode(isFavoriteMode: Boolean) {
        _isFavoriteMode.value = isFavoriteMode
    }
}