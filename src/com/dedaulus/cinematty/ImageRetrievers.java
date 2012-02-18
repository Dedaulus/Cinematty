package com.dedaulus.cinematty;

import com.dedaulus.cinematty.framework.FrameImageRetriever;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.PosterImageRetriever;

/**
 * User: Dedaulus
 * Date: 12.02.12
 * Time: 17:22
 */
public interface ImageRetrievers {
    MovieImageRetriever getMovieImageRetriever();

    MovieImageRetriever getMovieSmallImageRetriever();

    FrameImageRetriever getFrameImageRetriever();

    PosterImageRetriever getPosterImageRetriever();
}
