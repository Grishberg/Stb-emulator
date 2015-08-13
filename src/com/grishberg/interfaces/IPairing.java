package com.grishberg.interfaces;

import com.grishberg.data.model.PairingInfo;
import com.grishberg.data.model.Profile;

/**
 * Created by g on 13.08.15.
 */
public interface IPairing {
    Profile getProfile(String token);
}
