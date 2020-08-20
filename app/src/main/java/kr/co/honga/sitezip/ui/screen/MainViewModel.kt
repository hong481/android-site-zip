package kr.co.honga.sitezip.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.co.honga.sitezip.base.viewmodel.BaseViewModel
import kr.co.honga.sitezip.data.local.entity.Site
import kr.co.honga.sitezip.data.local.entity.SiteType
import kr.co.honga.sitezip.firebase.FireBaseDatabaseUtil

class MainViewModel(

    private val fireBaseDatabaseUtil: FireBaseDatabaseUtil

) : BaseViewModel() {

    companion object {
        const val TAG: String = "MainViewModel"
    }

    private val _siteTypes: MutableLiveData<MutableList<SiteType>> = MutableLiveData()

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

    fun getSites(
        onComplete : ((siteTypes: ArrayList<SiteType>) -> Unit)
    ) {
        showProgress()
        fireBaseDatabaseUtil.database.getReference(FireBaseDatabaseUtil.SITES_PATH)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, dataSnapshot.childrenCount.toString())

                    val siteTypes: ArrayList<SiteType> = arrayListOf()

                    for (typeSnapshot: DataSnapshot in dataSnapshot.children) {
                        val siteType: SiteType? =
                            typeSnapshot.getValue(SiteType::class.java).apply {
                                this?.typeName = typeSnapshot.key ?: ""
                            }
                        siteType?.let {
                            typeSnapshot.children.forEach { dataSnapshot ->
                                val site: Site? = dataSnapshot.getValue(Site::class.java)
                                site?.let { parsingSite ->
                                    siteType.siteList.add(parsingSite)
                                }
                            }
                            siteTypes.add(it)
                        }
                    }
                    _siteTypes.value = siteTypes
                    if(siteTypes.size > 0) {
                        onComplete(siteTypes)
                    }
                    dismissProgress()
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
}