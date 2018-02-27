package br.com.darksite.jazze.adapteur;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import br.com.darksite.jazze.fragments.ChatFragment;
import br.com.darksite.jazze.fragments.FriendsFragment;
import br.com.darksite.jazze.fragments.RequestFragment;

public class OngletsPageAdapter extends FragmentPagerAdapter{



    public OngletsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    //Trois fragments, alors, le return devrait etre 3
    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Demandes";

            case 1:
                return "Jaser";

            case 2:
                return "Mes amis";

            default:
                return null;
        }
    }

}
