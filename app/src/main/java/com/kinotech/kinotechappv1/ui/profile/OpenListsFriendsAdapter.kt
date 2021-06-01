package com.kinotech.kinotechappv1.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kinotech.kinotechappv1.R
import com.kinotech.kinotechappv1.ui.lists.AnyItemInAdapterList
import com.kinotech.kinotechappv1.ui.lists.ListOfMovieFragment
import com.kinotech.kinotechappv1.ui.lists.ListsFragment
import com.kinotech.kinotechappv1.ui.search.FilmPageFragment
import com.kinotech.kinotechappv1.ui.search.Genres
import com.kinotech.kinotechappv1.ui.search.SimpleResult


class OpenListsFriendsAdapter(
    private val mData: ArrayList<AnyItemInAdapterList.ButtonShowList>,
    val context: Context,
    private val subsInfo: SubsInfo
) :
    RecyclerView.Adapter<OpenListsFriendsAdapter.MyViewHolder>() {
    var mInflater: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = mInflater.inflate(R.layout.open_friend_list_film, parent, false)
        return MyViewHolder(view, subsInfo)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        return holder.bind(mData[position])
    }

    class MyViewHolder(itemView: View, private val subsInfo: SubsInfo) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        val filmCount: TextView = itemView.findViewById(R.id.film_count)
        val imgListH: ImageView = itemView.findViewById(R.id.img_list)
        val addBtn: ImageButton = itemView.findViewById(R.id.addBtn)

        private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        fun bind(lists: AnyItemInAdapterList.ButtonShowList) {
            var count: Int
//            Log.d("dbfav", "bind: ${movie.nameRu}")
            val options = RequestOptions()
            itemTitle.text = (lists as AnyItemInAdapterList.ButtonShowList).itemTitle
            subsInfo.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child(itemTitle.text.toString())
                    .child("Movies")
            }.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    count = snapshot.childrenCount.toInt()
                    Log.d("dbfav", "onDataChange: $count ")
                    filmCount.text = "$count фильмов"
                    if (filmCount.text == "0 фильмов") {
                        Glide
                            .with(itemView.context)
                            .load(lists.imgList)
                            .error(R.drawable.ic_baseline_movie_creation_24)
                            .into(imgListH)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            val photoRef = subsInfo.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child(itemTitle.text.toString())
                    .child("Movies")
            }
            val queryUid: Query = photoRef.orderByKey().limitToFirst(1)
            queryUid.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        try {
                            val result = snap.getValue(SimpleResult::class.java)!!
                            val imgList: String = result.posterUrlPreview
                            Log.d(
                                "dbImg",
                                "onDataChange3:${snapshot.childrenCount.toInt()} "
                            )
                            Glide
                                .with(itemView.context)
                                .load(imgList)
                                .error(R.drawable.ic_baseline_movie_creation_24)
                                .into(imgListH)

                        } catch (e: Exception) {
                            Log.d("dbfav", "onDataChange: $e")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
            Log.d("recyclerView  ", "${itemTitle.text}")
            checkAddedButton(addBtn)
            addBtn.setOnClickListener {
                if (addBtn.tag == "button not added"){
                    addList()
                }
                else{
                    deleteList()
                }
            }
        }

        private fun checkAddedButton(addButton: ImageButton) {
            subsInfo.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child("UserLists")
            }.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Lists")
                            .child(it1.toString())
                            .child("UserLists")
                    }.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snap: DataSnapshot) {
                            if (snap.child(itemTitle.text.toString()).exists()) {
                                addButton.tag = "button is added"
                                addButton.setBackgroundResource(R.drawable.ic_check)
                            } else {
                                addButton.tag = "button not added"
                                addButton.setBackgroundResource(R.drawable.ic_add)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        private fun addList() {
            subsInfo.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child(itemTitle.text.toString())
                    .child("Movies")
            }.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Lists")
                            .child(it1.toString())
                            .child("UserLists")
                            .child(itemTitle.text.toString())
                            .setValue(itemTitle.text.toString())
                    }
                    user?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Lists")
                            .child(it1.toString())
                            .child(itemTitle.text.toString())
                            .child("IsOpened")
                            .setValue(false)
                    }
                    for (snap in snapshot.children) {
                        val result = snap.getValue(SimpleResult::class.java)
                        user?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Lists")
                                .child(it1.toString())
                                .child(itemTitle.text.toString())
                                .child("Movies")
                                .child(snap.key.toString())
                                .setValue(result)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        private fun deleteList() {
            user?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child(itemTitle.text.toString())
                    .removeValue()
            }
            user?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Lists")
                    .child(it1.toString())
                    .child("UserLists")
                    .child(itemTitle.text.toString())
                    .removeValue()
            }
        }
    }
    }


