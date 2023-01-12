package io.homeassistant.companion.android.vehicle

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.toAndroidIconCompat
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import io.homeassistant.companion.android.common.data.integration.friendlyName
import io.homeassistant.companion.android.common.data.integration.friendlyState
import io.homeassistant.companion.android.common.data.integration.getIcon
import io.homeassistant.companion.android.common.data.integration.onPressed
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class EntityGridVehicleScreen(
    carContext: CarContext,
    val integrationRepository: IntegrationRepository,
    val title: String,
    val entities: MutableMap<String, Entity<*>>,
) : Screen(carContext) {

    companion object {
        private const val TAG = "EntityGridVehicleScreen"
    }

    init {
        lifecycleScope.launch {
            integrationRepository.getEntityUpdates()?.collect { entity ->
                if (entities.containsKey(entity.entityId)) {
                    entities[entity.entityId] = entity
                    invalidate()
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        entities.forEach { (entityId, entity) ->
            val icon = entity.getIcon(carContext) ?: CommunityMaterial.Icon.cmd_cloud_question
            listBuilder.addItem(
                GridItem.Builder()
                    .setLoading(false)
                    .setTitle(entity.friendlyName)
                    .setText(entity.friendlyState)
                    .setImage(
                        CarIcon.Builder(IconicsDrawable(carContext, icon).toAndroidIconCompat())
                            .setTint(CarColor.DEFAULT)
                            .build()
                    )
                    .setOnClickListener {
                        Log.i(TAG, "$entityId clicked")
                        lifecycleScope.launch {
                            entity.onPressed(integrationRepository)
                        }
                    }
                    .build()
            )
        }

        return GridTemplate.Builder()
            .setTitle(title)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}