package com.example.mysportapplication.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mysportapplication.AccountSettingsActivity
import com.example.mysportapplication.Model.User
import com.example.mysportapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*

//import kotlinx.android.synthetic.main.fragment_profile.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        if(profileId == firebaseUser.uid){
            view.edit_account_settings_btn.text = "Edit Profile"
        }
        else if(profileId != firebaseUser.uid){
            checkFollowAndFollowingButtonStatus()
        }

        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()

            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Add friend" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Add friend").child(it1.toString())
                            .child("Your friend").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Add friend").child(profileId)
                            .child("Your friends").child(it1.toString())
                            .setValue(true)
                    }
                }

                getButtonText == "Your friend" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Add friend").child(it1.toString())
                            .child("Your friend").child(profileId)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Add friend").child(profileId)
                            .child("Your friends").child(it1.toString())
                            .removeValue()
                    }
                }
            }
                    //startActivity(Intent(context, AccountSettingsActivity::class.java))
        }

        getFollowers()
        getFollowings()
        userInfo()
        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
            val followingRef = firebaseUser?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Add friend").child(it1.toString())
                    .child("Your friend")
            }
        if(followingRef != null){
            followingRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileId).exists()){
                        view?.edit_account_settings_btn?.text  = "Your friend"
                    }
                    else{
                        view?.edit_account_settings_btn?.text = "Add friend"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers(){
        val followersRef =
            FirebaseDatabase.getInstance().reference
                .child("Add friend").child(profileId)
                .child("Your friends")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowings(){
        val followersRef =
            FirebaseDatabase.getInstance().reference
                .child("Add friend").child(profileId)
                .child("Your friend")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text = user!!.getUsername()
                    view?.full_name_profile_frag?.text = user!!.getFullname()
                    view?.bio_profile_frag?.text = user!!.getBio()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}