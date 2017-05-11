package me.jbusdriver.mvp.presenter

import android.net.Uri
import com.cfzx.utils.CacheLoader
import io.reactivex.Flowable
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MovieListContract
import me.jbusdriver.mvp.bean.Movie
import me.jbusdriver.mvp.model.BaseModel
import me.jbusdriver.ui.data.DataSourceType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Administrator on 2017/5/10 0010.
 */
class SimpleMovieListPresenter(val url: String) : AbstractRefreshLoadMorePresenterImpl<MovieListContract.MovieListView>(), MovieListContract.MovieListPresenter {

    val host by lazy {
        Uri.parse(url).let {
            checkNotNull(it)
            "${it.scheme}://${it.host}"
        }
    }

    override fun loadAll(iaAll: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*不需要*/
    override val model: BaseModel<Int, String> = object : BaseModel<Int, String> {
        override fun requestFor(t: Int) = Flowable.fromCallable {
            (if (t == 1) url else "$host${pageInfo.nextPath}").let {
                KLog.d("fromCallable page $pageInfo requestFor : $it")
                Jsoup.connect(it).get().toString()
            }

        }.doOnNext {
            if (t == 1) CacheLoader.lru.put(url, it)
        }

        override fun requestFromCache(t: Int): Flowable<String> = Flowable.concat(CacheLoader.justLru(url), requestFor(t))

    }


    override fun stringMap(str: Document) = Movie.loadFromDoc(mView?.type ?: DataSourceType.CENSORED, str)

    override fun onRefresh() {
        CacheLoader.lru.remove(url)
        super.onRefresh()
    }

}