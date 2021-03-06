/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.broadcast.content;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import me.zhanghai.android.douya.network.api.info.Broadcast;
import me.zhanghai.android.douya.settings.info.Settings;
import me.zhanghai.android.douya.util.Callback;
import me.zhanghai.android.douya.util.FragmentUtils;

public class HomeBroadcastListResource extends BroadcastListResource {

    private static final String FRAGMENT_TAG_DEFAULT = HomeBroadcastListResource.class.getName();

    private final Handler mHandler = new Handler();

    private boolean mStopped;

    private static HomeBroadcastListResource newInstance() {
        //noinspection deprecation
        HomeBroadcastListResource resource = new HomeBroadcastListResource();
        resource.setArguments(null, null);
        return resource;
    }

    public static HomeBroadcastListResource attachTo(FragmentActivity activity, String tag,
                                                     int requestCode) {
        return attachTo(activity, tag, true, null, requestCode);
    }

    public static HomeBroadcastListResource attachTo(FragmentActivity activity) {
        return attachTo(activity, FRAGMENT_TAG_DEFAULT, REQUEST_CODE_INVALID);
    }

    public static HomeBroadcastListResource attachTo(Fragment fragment, String tag,
                                                     int requestCode) {
        return attachTo(fragment.getActivity(), tag, false, fragment, requestCode);
    }

    public static HomeBroadcastListResource attachTo(Fragment fragment) {
        return attachTo(fragment, FRAGMENT_TAG_DEFAULT, REQUEST_CODE_INVALID);
    }

    private static HomeBroadcastListResource attachTo(FragmentActivity activity, String tag,
                                                      boolean targetAtActivity,
                                                      Fragment targetFragment, int requestCode) {
        HomeBroadcastListResource resource = FragmentUtils.findByTag(activity, tag);
        if (resource == null) {
            resource = newInstance();
            if (targetAtActivity) {
                resource.targetAtActivity(requestCode);
            } else {
                resource.targetAtFragment(targetFragment, requestCode);
            }
            FragmentUtils.add(resource, activity, tag);
        }
        return resource;
    }

    /**
     * @deprecated Use {@code attachTo()} instead.
     */
    @SuppressWarnings("deprecation")
    public HomeBroadcastListResource() {}

    @Override
    public void onStart() {
        super.onStart();

        mStopped = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        mStopped = true;

        List<Broadcast> broadcastList = get();
        if (broadcastList != null && broadcastList.size() > 0) {
            saveToCache(broadcastList);
        }
    }

    @Override
    protected void loadOnStart() {
        loadFromCache();
    }

    private void loadFromCache() {

        setLoading(true);

        HomeBroadcastListCache.get(mHandler, new Callback<List<Broadcast>>() {
            @Override
            public void onValue(List<Broadcast> broadcastList) {
                onLoadFromCacheComplete(broadcastList);
            }
        }, getActivity());
    }

    private void onLoadFromCacheComplete(List<Broadcast> broadcastList) {

        setLoading(false);

        if (mStopped) {
            return;
        }

        boolean hasCache = broadcastList != null && broadcastList.size() > 0;
        if (hasCache) {
            set(broadcastList);
        }

        if (!hasCache || Settings.AUTO_REFRESH_HOME.getValue(getActivity())) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStopped) {
                        return;
                    }
                    // TODO: Handle the case when cache is loaded, but auto refresh is started, so
                    // that swipe refresh should be shown by us.
                    load(false);
                }
            });
        }
    }

    private void saveToCache(List<Broadcast> broadcastList) {
        HomeBroadcastListCache.put(broadcastList, getActivity());
    }
}
