https://github.com/tchapi/markdown-cheatsheet
https://raw.githubusercontent.com/android/sunflower/main/README.md

PlantKeeper
===========

PlantKeeper is a simple app written on Koltin that helps you taking care of your plants.

Introduction
------------

PlantsKeeper's GooglePlay page: https://play.google.com/store/apps/details?id=com.goranatos.plantkeeper

App's features
------------

* light/dark themes
* 3 languages: English, Russian, Spanish
* 

Getting Started
---------------
This project uses the Gradle build system. To build this project, use the
`gradlew build` command or use "Import Project" in Android Studio.

There are two Gradle tasks for testing the project:
* `connectedAndroidTest` - for running Espresso on a connected device
* `test` - for running unit tests

Screenshots: light theme
-----------
<p float="center"> 
  <img src="screenshots/light_my_plants_linear.png" alt="Your image title" width="300">
  <img src="screenshots/light_edit_plant.png" alt="Your image title" width="300">
  <img src="screenshots/light_plants_info.png" alt="Your image title" width="300">
</p>


Screenshots: dark theme
------------
<p float="left"> 
  <img src="screenshots/dark_edit_plant.png" alt="Your image title" width="300">
  <img src="screenshots/dark_my_plants_grid.png" alt="Your image title" width="300">
  <img src="screenshots/dark_plant_info.png" alt="Your image title" width="300">
</p>

Libraries Used
--------------

* SharedPreferences
* ViewModel
* LiveData
* Room
* Navigation
* MaterialDesign
* Kotlin Android Coroutines
* Lifecycles
* Broadcasts
* Animations and Transitions
* [Firebase: Performance, Crashlitics, Analitics][0] for analize users app's performance, crashes and getting statistics
* [Hilt][1] for dependency injection
* [Glide][2] for image loading
* [Intro][3] on first start shop Intro's slides 
* [Permission][4] for getting permissions
* [lingver][5] for changing language
* [uCrop][6] for cropping image when getting plant's photos
* [groupie][7] for recycle view easy usage

[0]: https://firebase.google.com/
[1]: https://dagger.dev/hilt/
[2]: https://bumptech.github.io/glide/
[3]: https://github.com/AppIntro/AppIntro
[4]: https://github.com/permissions-dispatcher/PermissionsDispatcher
[5]: https://github.com/YarikSOffice/lingver
[6]: https://github.com/Yalantis/uCrop
[7]: https://github.com/lisawray/groupie

License
-------

  https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
