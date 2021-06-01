package com.kinotech.kinotechappv1.ui.profile.subs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kinotech.kinotechappv1.databinding.SubscribersFragmentBinding
import com.kinotech.kinotechappv1.ui.profile.SubsInfo

class SubscribersFragment : Fragment() {

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var subsRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    val result = arrayListOf<SubsInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SubscribersFragmentBinding.inflate(inflater, container, false)
        val followersRef = user?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(it1.toString())
                .child("Followers")
        }.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                result.clear()
                for(snap in snapshot.children){
                    val userInfoRef = user?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child(snap.value.toString())
                    }.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snaps: DataSnapshot) {
                            snaps.getValue(SubsInfo::class.java)?.let { result.add(it) }
                            Log.d("snapinfo", "onDataChange: ${snaps.getValue(SubsInfo::class.java)}")
                            binding.subscribersRV.apply {
                                setHasFixedSize(true)
                                layoutManager = LinearLayoutManager(context)
                                adapter = SubscribersAdapter(result)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                    Log.d("followerlist", "onDataChange: $result")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        return binding.root
    }

}