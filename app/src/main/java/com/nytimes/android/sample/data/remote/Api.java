package com.nytimes.android.sample.data.remote;

import com.nytimes.android.sample.data.model.RedditData;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface Api {

    @GET("r/{subredditName}/new/.json")
    Observable<RedditData> fetchSubreddit(@Path("subredditName") String subredditName,
                                          @Query("limit") String limit);

    @GET("r/{subredditName}/new/.json")
    Observable<Response> fetchSubredditForPersister(@Path("subredditName") String subredditName,
                                        @Query("limit") String limit);
}
