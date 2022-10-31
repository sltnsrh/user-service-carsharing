package com.intern.carsharing.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlackList implements Serializable {
    private long userId;
    private String jwtToken;
}
