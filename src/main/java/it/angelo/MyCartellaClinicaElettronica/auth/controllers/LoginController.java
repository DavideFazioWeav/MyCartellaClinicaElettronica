package it.angelo.MyCartellaClinicaElettronica.auth.controllers;

import it.angelo.MyCartellaClinicaElettronica.appointment.controllers.AppointmentController;
import it.angelo.MyCartellaClinicaElettronica.auth.entities.LoginDTO;
import it.angelo.MyCartellaClinicaElettronica.auth.entities.LoginRTO;
import it.angelo.MyCartellaClinicaElettronica.auth.services.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * create an API for handling login system
 */
//annotiamo la classe come RestController e mappiamo su /auth
@RestController
@RequestMapping("/auth")
public class LoginController {

    //Iniettiamo il loginService
    @Autowired
    private LoginService loginService;

    Logger logger = LoggerFactory.getLogger(LoginController.class);
    int lineGetter = new Exception().getStackTrace()[0].getLineNumber();

    //mappiamo il metodo sul percorso /login

    /**
     * @param loginDTO contains login info to transfer
     * @return loginRTO that contain a user and a JWT Token
     * @throws Exception a generic exception can be thrown if loginRTO is null
     */
    @PostMapping("/login")
    //creiamo un metodo che prende in ingresso un LoginDTO e ci restituisce un LoginRTO
    public LoginRTO login(@RequestBody LoginDTO loginDTO)throws Exception{
        logger.debug(String.format("@PostMapped \'/login\' method called at %s at line# %d by %s",
                LoginController.class , lineGetter, loginDTO.getEmail()));
        if (loginDTO == null) throw new NullPointerException("loginDTO is null.");
        //invochiamo il metodo login presente nella classe LoginService
        LoginRTO loginRTO = loginService.login(loginDTO);
        //lanciamo un eccezione generica se loginRTO è nullo
        if(loginRTO == null) throw new Exception("Cannot login");
        return loginRTO;
    }
}