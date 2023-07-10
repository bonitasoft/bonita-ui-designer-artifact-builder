/** 
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.common.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.common.livebuild.Watcher;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class FragmentRepositoryTest {

    @TempDir
    Path temporaryFolder;
    //The persister is not mocked
    private JsonFileBasedPersister<Fragment> persister;

    private JsonFileBasedLoader<Fragment> loader;

    private FragmentRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        final BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
        JsonHandler jsonHandler = new JsonHandlerFactory().create();
        persister = Mockito
                .spy(new JsonFileBasedPersister<>(jsonHandler, validator, null, null));
        loader = Mockito.spy(new JsonFileBasedLoader<>(jsonHandler, Fragment.class));
        repository = new FragmentRepository(
                temporaryFolder,
                temporaryFolder,
                persister,
                loader,
                validator,
                Mockito.mock(Watcher.class));
    }

    private Fragment addToRepository(Fragment fragment) throws Exception {
        //A fragment is in its own folder
        Path repo = Files.createDirectory(temporaryFolder.resolve(fragment.getId()));
        persister.save(repo, fragment);
        return fragment;
    }

    private Fragment addToRepository(FragmentBuilder fragment) throws Exception {
        return addToRepository(fragment.build());
    }

    @Test
    public void should_get_a_fragment_from_a_json_file_repository() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        addToRepository(expectedFragment);

        Fragment fetchedFragment = repository.get(expectedFragment.getId());

        assertThat(fetchedFragment).isEqualTo(expectedFragment);
    }

    @Test
    public void should_throw_NotFoundException_when_getting_an_inexisting_fragment() throws Exception {
        assertThrows(NotFoundException.class, () -> repository.get("fragment-id-unknown"));
    }

    @Test
    public void should_get_all_fragment_from_repository_empty() {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    public void should_get_all_fragment_from_repository() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        addToRepository(expectedFragment);

        List<Fragment> fetchedFragments = repository.getAll();

        assertThat(fetchedFragments).containsExactly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_itself() throws Exception {
        Fragment itself = addToRepository(FragmentBuilder.aFragment().withId("aFragment"));
        Fragment expectedFragment = addToRepository(FragmentBuilder.aFragment().withId("anotherFragment"));

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_those_which_are_using_it() throws Exception {
        Fragment expectedFragment = addToRepository(FragmentBuilder.aFragment().withId("anotherFragment").build());
        Fragment itself = addToRepository(FragmentBuilder.aFragment().withId("aFragment"));
        addToRepository(FragmentBuilder.aFragment().with(itself).build());

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_those_which_are_using_a_fragment_that_use_itself() throws Exception {
        Fragment expectedFragment = addToRepository(FragmentBuilder.aFragment().withId("anotherFragment").build());
        Fragment itself = addToRepository(FragmentBuilder.aFragment().withId("aFragment").build());
        Fragment container = addToRepository(FragmentBuilder.aFragment().with(itself));
        Fragment container2 = addToRepository(FragmentBuilder.aFragment().with(container));
        addToRepository(FragmentBuilder.aFragment().with(container2));

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_save_a_fragment_in_a_json_file_repository() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");

        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
        repository.updateLastUpdateAndSave(expectedFragment);

        //A json file has to be created in the repository
        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).exists();
        Assertions.assertThat(expectedFragment.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    public void should_save_a_page_without_updating_last_update_date() {
        Fragment fragment = repository
                .updateLastUpdateAndSave(
                        FragmentBuilder.aFragment().withId("customLabel").withName("theFragmentName").build());
        Instant lastUpdate = fragment.getLastUpdate();

        fragment.setName("newName");
        repository.save(fragment);

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.getLastUpdate()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(fetchedFragment.getName()).isEqualTo("newName");
    }

    @Test
    public void should_throw_RepositoryException_when_error_occurs_while_saving_a_fragment() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        Mockito.doThrow(new IOException()).when(persister)
                .save(temporaryFolder.resolve(expectedFragment.getId()), expectedFragment);

        assertThrows(RepositoryException.class, () -> repository.updateLastUpdateAndSave(expectedFragment));
    }

    @Test
    public void should_throw_IllegalArgumentException_while_saving_a_fragment_with_no_id_set() {
        Fragment expectedFragment = FragmentBuilder.aFragment().withId(null).build();
        assertThrows(IllegalArgumentException.class, () -> repository.updateLastUpdateAndSave(expectedFragment));
    }

    @Test
    public void should_throw_ConstraintValidationException_while_saving_a_fragment_with_bad_name() {
        Fragment expectedFragment = FragmentBuilder.aFragment().withId("fragment-id").withName("éé&é&z").build();
        assertThrows(ConstraintValidationException.class, () -> repository.updateLastUpdateAndSave(expectedFragment));
    }

    @Test
    public void should_save_all_fragment_in_a_json_file_repository() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
        repository.saveAll(Arrays.asList(expectedFragment));
        //A json file has to be created in the repository
        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).exists();
        Assertions.assertThat(expectedFragment.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    public void should_not_thrown_NPE_on_save_all_fragment_when_list_null() {
        repository.saveAll(null);
    }

    @Test
    public void should_delete_a_fragment_with_his_json_file_repository() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        addToRepository(expectedFragment);
        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).exists();
        repository.delete(expectedFragment.getId());
        Assertions.assertThat(temporaryFolder.resolve(expectedFragment.getId())
                .resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
    }

    @Test
    public void should_throw_NotFoundException_when_deleting_inexisting_fragment() {
        assertThrows(NotFoundException.class, () -> repository.delete("foo"));
    }

    @Test
    public void should_throw_RepositoryException_when_error_occurs_on_object_included_search_list() throws Exception {
        Fragment expectedFragment = FragmentBuilder.aFilledFragment("fragment-id");
        Mockito.doThrow(new IOException()).when(loader).findByObjectId(temporaryFolder,
                expectedFragment.getId());

        assertThrows(RepositoryException.class, () -> repository.findByObjectId(expectedFragment.getId()));

    }

    @Test
    public void should_mark_a_widget_as_favorite() throws Exception {
        Fragment fragment = addToRepository(FragmentBuilder.aFragment().notFavorite());

        repository.markAsFavorite(fragment.getId());

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.isFavorite()).isTrue();

    }

    @Test
    public void should_unmark_a_widget_as_favorite() throws Exception {
        Fragment fragment = addToRepository(FragmentBuilder.aFragment().favorite());

        repository.unmarkAsFavorite(fragment.getId());

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.isFavorite()).isFalse();
    }

    @Test
    public void should_keep_fragment_name_id_if_there_is_no_fragment_with_same_id() throws Exception {
        String newFragmentId = repository.getNextAvailableId("newFragmentId");

        assertThat(newFragmentId).isEqualTo("newFragmentId");
    }
}
