package ch.epfl.sdp.ui.searchgroupselection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.SearchGroupData

class SearchgroupViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.searchgroup_selection_recyclerview_item, parent, false)) {
    private var name: TextView? = itemView.findViewById(R.id.searchGroupItemName)

    fun bind(searchGroup: SearchGroupData) {
        name?.text = searchGroup.name
    }

}