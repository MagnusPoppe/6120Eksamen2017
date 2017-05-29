package no.byteme.magnuspoppe.eksamen.datamodel;

import java.util.ArrayList;

/**
 * Created by MagnusPoppe on 29/05/2017.
 */

public class User
{
    // Brukerens attributter:
    private String email;
    private String firstname;
    private String lastname;
    private int age;
    ArrayList<Destination> userLocations;

    public User(String JsonData)
    {

    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }
}
