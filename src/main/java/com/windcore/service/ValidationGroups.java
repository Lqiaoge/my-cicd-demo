package com.windcore.service;

import jakarta.validation.groups.Default;

public interface ValidationGroups {

    interface CreateGroup extends Default {
    }
    interface UpdateGroup extends  Default{
    }
}
