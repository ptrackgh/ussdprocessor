/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qos.ussd.util.padme.dto;

//import com.qos.ussd.util.padme.dto.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Malkiel
 */
public class TUsUsuarios {
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("telephone")
    @Expose
    private int telephone;
    @SerializedName("codUsuario")
    @Expose
    private String codUsuario;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        this.codUsuario = codUsuario;
    }

    public TUsUsuarios(String email, int telephone, String codUsuario) {
        this.email = email;
        this.telephone = telephone;
        this.codUsuario = codUsuario;
    }

    @Override
    public String toString() {
        return "TUsUsuarios{" + "email=" + email + ", telephone=" + telephone + ", codUsuario=" + codUsuario + '}';
    }

    
    
}
